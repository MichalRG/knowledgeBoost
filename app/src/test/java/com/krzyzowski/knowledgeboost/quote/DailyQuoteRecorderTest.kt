package com.krzyzowski.knowledgeboost.quote

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.krzyzowski.knowledgeboost.content.ContentBuilder
import com.krzyzowski.knowledgeboost.content.FakeTopicFolder
import com.krzyzowski.knowledgeboost.content.Library
import com.krzyzowski.knowledgeboost.content.TopicFile
import com.krzyzowski.knowledgeboost.content.TopicFolderSource
import com.krzyzowski.knowledgeboost.content.TopicLibraryStore
import com.krzyzowski.knowledgeboost.data.DailyQuoteHistoryStore
import com.krzyzowski.knowledgeboost.data.DailyQuoteStore
import com.krzyzowski.knowledgeboost.data.TopicFolderStore
import com.krzyzowski.knowledgeboost.time.FakeClock
import com.krzyzowski.knowledgeboost.time.Today
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class DailyQuoteRecorderTest {
    @get:Rule
    val folder = TemporaryFolder()
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val main by lazy {
        PreferenceDataStoreFactory.create(scope = scope) { File(folder.root, "main.preferences_pb") }
    }
    private val historyData by lazy {
        PreferenceDataStoreFactory.create(scope = scope) { File(folder.root, "history.preferences_pb") }
    }
    private val anchor by lazy { DailyQuoteStore(main) }
    private val store by lazy { DailyQuoteHistoryStore(historyData) }
    private val start = LocalDate.parse("2026-11-05")
    private val clock = FakeClock(Instant.parse("2026-11-09T12:00:00Z"))
    private val today = Today(clock)
    private val bundled = Library(emptyList(), listOf(ContentBuilder.quote("Bundled")))
    private val source = FakeTopicFolder().apply {
        folders["folder"] = listOf(ContentBuilder.quoteFile("Folder first", "Folder second"))
    }
    private val library by lazy { TopicLibraryStore(bundled, TopicFolderStore(main), source) }

    @After
    fun closeDataStores() {
        scope.cancel()
    }

    @Test
    fun `initial history waits for the selected folder and never snapshots the temporary bundled list`() = runTest {
        anchor.start(start)
        TopicFolderStore(main).set("folder")
        val entered = CompletableDeferred<Unit>()
        val release = CountDownLatch(1)
        val delayedSource = object : TopicFolderSource by source {
            override fun read(uri: String): List<TopicFile> {
                entered.complete(Unit)
                check(release.await(5, TimeUnit.SECONDS)) { "Test folder was not released" }
                return source.read(uri)
            }
        }
        val delayed = TopicLibraryStore(bundled, TopicFolderStore(main), delayedSource)
        val recorder = DailyQuoteRecorder(store, anchor, delayed, today)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { recorder.run() }
        recorder.state.first { it.history != null }
        assertTrue(store.history.first().days.isEmpty())
        val reload = backgroundScope.launch { delayed.reload() }
        try {
            entered.await()
            assertTrue(delayed.state.value.loading)
            assertFalse(delayed.state.value.initialized)
            assertTrue(store.history.first().days.isEmpty())
        } finally {
            release.countDown()
        }
        reload.join()
        val saved = requireNotNull(recorder.state.first { it.history?.days?.size == 5 }.history)
        repeat(5) { offset ->
            assertEquals(if (offset % 2 == 0) "Folder first" else "Folder second", saved.days[start.plusDays(offset.toLong())]?.text)
        }
        assertEquals(start, anchor.startedOn.first())
    }

    @Test
    fun `failed initial folder loading reports an error without freezing incorrect history and reload recovers`() = runTest {
        anchor.start(start)
        TopicFolderStore(main).set("folder")
        source.readError = IOException("Folder unavailable")
        library.reload()
        val recorder = DailyQuoteRecorder(store, anchor, library, today)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { recorder.run() }

        assertEquals("Folder unavailable", recorder.state.first { it.error != null }.error)
        assertTrue(store.history.first().days.isEmpty())

        source.readError = null
        library.reload()
        val recovered = requireNotNull(recorder.state.first { it.error == null && it.history?.days?.size == 5 }.history)
        assertEquals("Folder first", recovered.days[start]?.text)
    }

    @Test
    fun `cold start folder errors preserve the last known schedule and disconnect applies bundled quotes only tomorrow`() = runTest {
        anchor.start(start)
        library.select("folder")
        store.record(start, start, library.library.value.quotes)
        source.readError = IOException("Folder unavailable")
        val restarted = TopicLibraryStore(bundled, TopicFolderStore(main), source)
        restarted.reload()
        val recorder = DailyQuoteRecorder(store, anchor, restarted, today)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { recorder.run() }
        val saved = requireNotNull(recorder.state.first { it.history?.days?.size == 5 }.history)
        assertEquals("Folder first", saved.days[today.date.value]?.text)
        assertEquals(listOf("Folder first", "Folder second"), saved.quotes.map { it.text })
        assertNotNull(restarted.state.value.error)

        restarted.disconnect()
        recorder.state.first { it.history?.quotes == bundled.quotes }
        assertEquals(saved.days, recorder.state.value.history?.days)
        clock.instant = Instant.parse("2026-11-10T00:00:00Z")
        today.refresh()
        val next = requireNotNull(recorder.state.first { it.history?.days?.size == 6 }.history)
        assertEquals("Bundled", next.days[today.date.value]?.text)
        assertEquals("Folder first", next.days[start.plusDays(4)]?.text)
    }

    @Test
    fun `read and write failures are explicit quote history errors`() = runTest {
        library.reload()
        val failures = listOf(
            flow<Preferences> { throw IOException("Cannot read history") } to "Cannot read history",
            flowOf(emptyPreferences()) to "Cannot save history",
        )
        failures.forEach { (values, message) ->
            val failing = object : DataStore<Preferences> {
                override val data: Flow<Preferences> = values
                override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
                    throw IOException("Cannot save history")
            }
            val recorder = DailyQuoteRecorder(DailyQuoteHistoryStore(failing), anchor, library, today)
            recorder.run()
            assertEquals(message, recorder.state.value.error)
        }
    }

    @Test
    fun `malformed persisted history is reported and never overwritten by a reconstructed success`() = runTest {
        val key = stringPreferencesKey("daily_quote_history_v1")
        historyData.edit { it[key] = "broken" }
        val recorder = DailyQuoteRecorder(store, anchor, library, today)

        recorder.run()

        assertNotNull(recorder.state.value.error)
        assertEquals("broken", historyData.data.first()[key])
    }
}
