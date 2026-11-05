package com.krzyzowski.knowledgeboost.content

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.krzyzowski.knowledgeboost.data.ProgressStore
import com.krzyzowski.knowledgeboost.data.ResumeStore
import com.krzyzowski.knowledgeboost.data.TopicFolderStore
import com.krzyzowski.knowledgeboost.time.FakeClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import java.time.Instant
import java.time.ZoneOffset

class TopicLibraryStoreTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val dataStore by lazy {
        PreferenceDataStoreFactory.create(scope = scope) { File(folder.root, "topics.preferences_pb") }
    }
    private val settings by lazy { TopicFolderStore(dataStore) }
    private val bundled = ContentBuilder.library(ContentBuilder.topic("bundled", listOf("known")))
    private val source = FakeTopicFolder()
    private val store by lazy { TopicLibraryStore(bundled, settings, source) }

    @After
    fun closeDataStore() {
        scope.cancel()
    }

    @Test
    fun `selecting a folder retains read access and saves it for a fresh library store`() = runTest {
        store.select("folder")

        assertEquals(listOf("folder"), source.retained)
        assertEquals("folder", settings.uri.first())
        assertEquals(1, store.state.value.topics)
        assertEquals(1, store.state.value.questions)
        assertNull(store.state.value.error)
        assertFalse(store.state.value.loading)
        val restarted = TopicLibraryStore(bundled, settings, source)
        restarted.reload()
        assertEquals(store.library.value.topics, restarted.library.value.topics)
    }

    @Test
    fun `reload applies category edits and deletions without changing bundled topics`() = runTest {
        store.select("folder")
        val before = store.library.value
        store.reload()
        assertSame(before, store.library.value)
        source.folders["folder"] = listOf(TopicFileBuilder.file(category = "English"))

        store.reload()

        assertTrue(store.library.value.categories.contains("English"))
        assertEquals(bundled.topic("bundled"), store.library.value.topic("bundled"))
        source.folders["folder"] = emptyList()
        store.reload()
        assertEquals(bundled.topics, store.library.value.topics)
        assertEquals(0, store.state.value.questions)
        assertEquals("folder", settings.uri.first())
    }

    @Test
    fun `a broken reload keeps the last good library and reports the failing filename`() = runTest {
        store.select("folder")
        val before = store.library.value
        source.folders["folder"] = listOf(TopicFileBuilder.file("valid"), TopicFile("broken.json", "{"))

        store.reload()

        assertSame(before, store.library.value)
        assertTrue(store.state.value.error.orEmpty().contains("broken.json"))
        assertEquals(1, store.state.value.questions)
        assertFalse(store.state.value.loading)
        source.folders["folder"] = listOf(TopicFileBuilder.file("fixed"))
        store.reload()
        assertNull(store.state.value.error)
        assertEquals(listOf("fixed", "bundled").sorted(), store.library.value.topics.map { it.id }.sorted())
    }

    @Test
    fun `an invalid replacement folder does not replace the saved selection or retain access`() = runTest {
        store.select("folder")
        val before = store.library.value
        source.folders["invalid"] = listOf(TopicFileBuilder.file(id = "known"))

        store.select("invalid")

        assertSame(before, store.library.value)
        assertEquals("folder", settings.uri.first())
        assertEquals("folder", store.state.value.uri)
        assertEquals(listOf("folder"), source.retained)
        assertTrue(store.state.value.error.orEmpty().contains("known"))
    }

    @Test
    fun `read failures and revoked permissions are visible after a cold start`() = runTest {
        settings.set("folder")
        listOf(IOException("Storage unavailable"), SecurityException("Permission revoked")).forEach { error ->
            source.readError = error
            val restarted = TopicLibraryStore(bundled, settings, source)

            restarted.reload()

            assertSame(bundled, restarted.library.value)
            assertEquals("folder", restarted.state.value.uri)
            assertEquals(error.message, restarted.state.value.error)
            assertFalse(restarted.state.value.loading)
        }
    }

    @Test
    fun `a failed permission grant does not save the selection or apply content`() = runTest {
        source.retainError = SecurityException("Read access denied")

        store.select("folder")

        assertNull(settings.uri.first())
        assertSame(bundled, store.library.value)
        assertEquals("Read access denied", store.state.value.error)
        assertFalse(store.state.value.loading)
    }

    @Test
    fun `switching and disconnecting release old access without clearing progress or resume positions`() = runTest {
        val progress = ProgressStore(dataStore, FakeClock(Instant.parse("2026-11-05T07:00:00Z"), ZoneOffset.UTC))
        val resume = ResumeStore(dataStore)
        store.select("folder")
        progress.missed("added-question")
        resume.set("added", "added-question")
        source.folders["other"] = listOf(TopicFileBuilder.file("other"))
        store.select("other")
        assertEquals(listOf("folder"), source.released)
        assertEquals("other", settings.uri.first())

        store.disconnect()

        assertEquals(listOf("folder", "other"), source.released)
        assertNull(settings.uri.first())
        assertNull(store.state.value.uri)
        assertEquals(bundled.topics, store.library.value.topics)
        assertTrue(progress.progress.first().of("added-question").missed)
        assertEquals("added-question", resume.positions.first().of("added"))
        store.select("folder")
        assertEquals("added-question", store.library.value.topic("added")?.questions?.single()?.id)
        assertTrue(progress.progress.first().of("added-question").missed)
    }
}
