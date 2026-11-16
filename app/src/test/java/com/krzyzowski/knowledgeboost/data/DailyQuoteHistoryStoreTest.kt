package com.krzyzowski.knowledgeboost.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.krzyzowski.knowledgeboost.content.ContentBuilder
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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.time.Instant
import java.time.LocalDate

class DailyQuoteHistoryStoreTest {
    @get:Rule
    val folder = TemporaryFolder()
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val file get() = File(folder.root, "history.preferences_pb")
    private val dataStore by lazy { PreferenceDataStoreFactory.create(scope = scope) { file } }
    private val store by lazy { DailyQuoteHistoryStore(dataStore) }
    private val day = LocalDate.parse("2026-11-05")
    private val quotes = listOf("First", "Second", "Third").map { ContentBuilder.quote(it) }

    @After
    fun closeDataStore() {
        scope.cancel()
    }

    @Test
    fun `fresh store has no history and recording backfills the existing start date`() = runTest {
        assertTrue(store.history.first().days.isEmpty())
        val saved = store.record(day, day.plusDays(3), quotes)
        assertEquals(4, saved.days.size)
        assertEquals(quotes[0], saved.days[day])
        assertEquals(quotes[0], saved.days[day.plusDays(3)])
        assertEquals(saved, store.history.first())
    }

    @Test
    fun `reopening the actual backing file preserves history and tomorrow's pending list`() = runTest {
        val firstJob = Job()
        val firstRun = PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + firstJob)) { file }
        val replacement = listOf(ContentBuilder.quote("Replacement"))
        val initial = DailyQuoteHistoryStore(firstRun).record(day, day.plusDays(3), quotes)
        val beforeRestart = DailyQuoteHistoryStore(firstRun).record(day, day.plusDays(3), replacement)
        firstJob.cancelAndJoin()

        assertEquals(beforeRestart, store.history.first())
        val next = store.record(day, day.plusDays(4), replacement)
        assertEquals(initial.days, next.days.filterKeys { !it.isAfter(day.plusDays(3)) })
        assertEquals(replacement.single(), next.days[day.plusDays(4)])
    }

    @Test
    fun `new history leaves original anchor study progress and topic resume positions untouched`() = runTest {
        val main = PreferenceDataStoreFactory.create(scope = scope) { File(folder.root, "main.preferences_pb") }
        val anchor = DailyQuoteStore(main)
        val progress = ProgressStore(main, FakeClock(Instant.parse("2026-11-05T12:00:00Z")))
        val resume = ResumeStore(main)
        anchor.start(day)
        progress.knewIt("known")
        progress.missed("missed")
        resume.set("topic", "known")
        val known = progress.progress.first().of("known")
        val missed = progress.progress.first().of("missed")

        store.record(requireNotNull(anchor.startedOn.first()), day.plusDays(4), quotes)

        assertEquals(day, anchor.startedOn.first())
        assertEquals(known, progress.progress.first().of("known"))
        assertEquals(missed, progress.progress.first().of("missed"))
        assertEquals("known", resume.positions.first().of("topic"))
    }
}
