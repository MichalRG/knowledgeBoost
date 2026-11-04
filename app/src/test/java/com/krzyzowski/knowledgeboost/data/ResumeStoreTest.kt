package com.krzyzowski.knowledgeboost.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.krzyzowski.knowledgeboost.schedule.QuestionProgress
import com.krzyzowski.knowledgeboost.schedule.Step
import com.krzyzowski.knowledgeboost.time.FakeClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class ResumeStoreTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val dataStoreScope = CoroutineScope(Dispatchers.IO + Job())

    private fun openDataStore(scope: CoroutineScope): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(scope = scope) { File(folder.root, "knowledgeboost.preferences_pb") }

    private val dataStore by lazy { openDataStore(dataStoreScope) }

    private val store by lazy { ResumeStore(dataStore) }

    @After
    fun closeDataStore() {
        dataStoreScope.cancel()
    }

    @Test
    fun `the question where the user stopped in each topic is still there after the app restarts`() = runTest {
        val firstRun = Job()
        val before = ResumeStore(openDataStore(CoroutineScope(Dispatchers.IO + firstRun)))
        before.set("redis", "redis-expiry")
        before.set("kotlin", "kotlin-data-class")
        firstRun.cancelAndJoin()

        val saved = store.positions.first()

        assertEquals("redis-expiry", saved.of("redis"))
        assertEquals("kotlin-data-class", saved.of("kotlin"))
        assertNull(saved.of("python"))
    }

    @Test
    fun `stopping again in a topic replaces its position and leaves the other topics alone`() = runTest {
        store.set("redis", "redis-persistence")
        store.set("kotlin", "kotlin-data-class")

        store.set("redis", "redis-expiry")

        val saved = store.positions.first()
        assertEquals("redis-expiry", saved.of("redis"))
        assertEquals("kotlin-data-class", saved.of("kotlin"))
    }

    @Test
    fun `a damaged resume entry is skipped and the others are kept`() = runTest {
        dataStore.edit {
            it[stringPreferencesKey("topic_resume_v1")] =
                "redis=redis-expiry,no-equals-sign,empty-question=,=empty-topic,kotlin=a=b,python=python-gil"
        }

        val saved = store.positions.first()

        assertEquals("redis-expiry", saved.of("redis"))
        assertEquals("python-gil", saved.of("python"))
        listOf("no-equals-sign", "empty-question", "kotlin", "").forEach { topicId ->
            assertNull("topic '$topicId'", saved.of(topicId))
        }
    }

    @Test
    fun `progress and resume positions saved in the same file do not overwrite each other`() = runTest {
        val progress = ProgressStore(dataStore, FakeClock(Instant.parse("2026-10-30T10:00:00Z"), ZoneOffset.UTC))

        progress.knewIt("redis-event-loop")
        store.set("redis", "redis-expiry")
        progress.missed("python-gil")
        store.set("kotlin", "kotlin-data-class")

        assertEquals(
            QuestionProgress(Step.DAYS_7, LocalDate.parse("2026-11-06"), missed = false, removed = false),
            progress.progress.first().of("redis-event-loop"),
        )
        assertTrue(progress.progress.first().of("python-gil").missed)
        assertEquals("redis-expiry", store.positions.first().of("redis"))
        assertEquals("kotlin-data-class", store.positions.first().of("kotlin"))
    }
}
