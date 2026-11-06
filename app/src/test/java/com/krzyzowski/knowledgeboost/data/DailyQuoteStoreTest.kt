package com.krzyzowski.knowledgeboost.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
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
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.time.Instant
import java.time.LocalDate

class DailyQuoteStoreTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val dataStore by lazy {
        PreferenceDataStoreFactory.create(scope = scope) { File(folder.root, "quotes.preferences_pb") }
    }
    private val store by lazy { DailyQuoteStore(dataStore) }
    private val day = LocalDate.parse("2026-11-05")

    @After
    fun closeDataStore() {
        scope.cancel()
    }

    @Test
    fun `first use saves the start day and later visits never reset it`() = runTest {
        assertNull(store.startedOn.first())
        store.start(day)
        store.start(day.plusDays(10))
        store.start(day.minusDays(1))

        assertEquals(day, store.startedOn.first())
    }

    @Test
    fun `the start date survives closing and reopening the backing datastore`() = runTest {
        val firstRun = Job()
        val firstDataStore = PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + firstRun)) {
            File(folder.root, "quotes.preferences_pb")
        }
        DailyQuoteStore(firstDataStore).start(day)
        firstRun.cancelAndJoin()

        assertEquals(day, store.startedOn.first())
    }

    @Test
    fun `starting the daily quote cycle preserves study progress and topic resume positions`() = runTest {
        val progress = ProgressStore(dataStore, FakeClock(Instant.parse("2026-11-05T12:00:00Z")))
        val resume = ResumeStore(dataStore)
        progress.knewIt("known-question")
        resume.set("topic", "known-question")
        val saved = progress.progress.first().of("known-question")

        store.start(day)

        assertEquals(saved, progress.progress.first().of("known-question"))
        assertEquals("known-question", resume.positions.first().of("topic"))
        assertEquals(day, store.startedOn.first())
    }
}
