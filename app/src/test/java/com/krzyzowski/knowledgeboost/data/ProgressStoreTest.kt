package com.krzyzowski.knowledgeboost.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.krzyzowski.knowledgeboost.content.ContentBuilder.library
import com.krzyzowski.knowledgeboost.content.ContentBuilder.topic
import com.krzyzowski.knowledgeboost.schedule.LibraryProgress
import com.krzyzowski.knowledgeboost.schedule.QuestionProgress
import com.krzyzowski.knowledgeboost.schedule.Step
import com.krzyzowski.knowledgeboost.session.CardQueue
import com.krzyzowski.knowledgeboost.time.FakeClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class ProgressStoreTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val dataStoreScope = CoroutineScope(Dispatchers.IO + Job())

    private val clock = FakeClock(Instant.parse("2026-10-30T10:00:00Z"), ZoneId.of("Europe/Warsaw"))

    private fun openDataStore(scope: CoroutineScope): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(scope = scope) { File(folder.root, "knowledgeboost.preferences_pb") }

    private val dataStore by lazy { openDataStore(dataStoreScope) }

    private val store by lazy { ProgressStore(dataStore, clock) }

    @After
    fun closeDataStore() {
        dataStoreScope.cancel()
    }

    private fun progress(step: Step?, hiddenUntil: String?, missed: Boolean = false, removed: Boolean = false) =
        QuestionProgress(step, hiddenUntil?.let(LocalDate::parse), missed, removed)

    private fun today(day: String) {
        clock.instant = Instant.parse("${day}T10:00:00Z")
    }

    private val redis = topic("redis", listOf("hidden-one", "open-one"))

    // The topic list on 30 Oct 2026, the day the clock starts on.
    private fun availableIds(progress: LibraryProgress) =
        CardQueue.available(redis, library(redis), progress, LocalDate.parse("2026-10-30")).cards.map { it.id }

    private fun missedIds(progress: LibraryProgress) = CardQueue.missed(library(redis), progress).cards.map { it.id }

    @Test
    fun `Knew it hides a question 7, 30, 90, 180 and 360 days and then 360 days again, counted from each answer day`() = runTest {
        val walk = listOf(
            Triple("2026-10-30", Step.DAYS_7, "2026-11-06"),
            Triple("2026-11-06", Step.DAYS_30, "2026-12-06"),
            Triple("2026-12-06", Step.DAYS_90, "2027-03-06"),
            Triple("2027-03-06", Step.DAYS_180, "2027-09-02"),
            Triple("2027-09-02", Step.DAYS_360, "2028-08-27"),
            Triple("2028-08-27", Step.DAYS_360, "2029-08-22"),
            Triple("2029-08-22", Step.DAYS_360, "2030-08-17"),
        )

        walk.forEach { (answeredOn, step, hiddenUntil) ->
            today(answeredOn)
            store.knewIt("redis-event-loop")

            assertEquals("after answering on $answeredOn", progress(step, hiddenUntil), store.progress.first().of("redis-event-loop"))
        }
    }

    @Test
    fun `a question is hidden until its day, then comes back and stays available with its step unchanged until it is answered`() = runTest {
        store.knewIt("redis-event-loop")
        val saved = store.progress.first().of("redis-event-loop")

        assertTrue(saved.isHiddenOn(LocalDate.parse("2026-11-05")))
        listOf("2026-11-06", "2026-11-07", "2026-12-30").forEach { day ->
            assertFalse("hidden on $day", saved.isHiddenOn(LocalDate.parse(day)))
        }
        assertEquals(progress(Step.DAYS_7, "2026-11-06"), saved)
    }

    @Test
    fun `a question answered weeks late moves one step up, counted from the day it was answered`() = runTest {
        store.knewIt("redis-event-loop")
        today("2026-12-20")

        store.knewIt("redis-event-loop")

        assertEquals(progress(Step.DAYS_30, "2027-01-19"), store.progress.first().of("redis-event-loop"))
    }

    @Test
    fun `Missed puts the question on the list, keeps it available and restarts the ladder at 7 days`() = runTest {
        store.knewIt("redis-event-loop")
        today("2026-11-06")
        store.knewIt("redis-event-loop")
        today("2026-11-10")

        store.missed("redis-event-loop")

        val missed = store.progress.first().of("redis-event-loop")
        assertEquals(progress(step = null, hiddenUntil = null, missed = true), missed)
        assertFalse(missed.isHiddenOn(LocalDate.parse("2026-11-10")))

        store.knewIt("redis-event-loop")

        assertEquals(progress(Step.DAYS_7, "2026-11-17", missed = true), store.progress.first().of("redis-event-loop"))
    }

    @Test
    fun `Knew it on a missed question keeps it listed and Knew it again while it is hidden changes nothing`() = runTest {
        store.missed("redis-event-loop")

        store.knewIt("redis-event-loop")

        val afterFirst = progress(Step.DAYS_7, "2026-11-06", missed = true)
        assertEquals(afterFirst, store.progress.first().of("redis-event-loop"))

        today("2026-11-02")
        store.knewIt("redis-event-loop")

        assertEquals(afterFirst, store.progress.first().of("redis-event-loop"))
    }

    @Test
    fun `Remove hides a question from its topic and the Missed list and Restore brings back its step, date and Missed flag`() = runTest {
        store.missed("hidden-one")
        store.knewIt("hidden-one")
        val before = store.progress.first().of("hidden-one")
        assertEquals(progress(Step.DAYS_7, "2026-11-06", missed = true), before)

        store.remove("hidden-one")
        store.remove("open-one")

        val removed = store.progress.first()
        assertEquals(before.copy(removed = true), removed.of("hidden-one"))
        assertEquals(emptyList<String>(), missedIds(removed))
        assertEquals(emptyList<String>(), availableIds(removed))

        store.restore("hidden-one")
        store.restore("open-one")

        val restored = store.progress.first()
        assertEquals(before, restored.of("hidden-one"))
        assertEquals(listOf("hidden-one"), missedIds(restored))
        assertEquals(listOf("open-one"), availableIds(restored))
    }

    @Test
    fun `Clear from Missed takes the question off the Missed list but leaves it in its topic with its step and date`() = runTest {
        store.missed("hidden-one")
        store.knewIt("hidden-one")
        store.missed("open-one")

        store.clearMissed("hidden-one")
        store.clearMissed("open-one")

        val cleared = store.progress.first()
        assertEquals(progress(Step.DAYS_7, "2026-11-06"), cleared.of("hidden-one"))
        assertEquals(progress(step = null, hiddenUntil = null), cleared.of("open-one"))
        assertEquals(emptyList<String>(), missedIds(cleared))
        assertEquals(listOf("open-one"), availableIds(cleared))
    }

    @Test
    fun `progress is still there after the app restarts`() = runTest {
        val firstRun = Job()
        val before = ProgressStore(openDataStore(CoroutineScope(Dispatchers.IO + firstRun)), clock)
        before.knewIt("redis-event-loop")
        before.missed("python-gil")
        before.remove("kotlin-data-class")
        firstRun.cancelAndJoin()

        val saved = store.progress.first()

        assertEquals(progress(Step.DAYS_7, "2026-11-06"), saved.of("redis-event-loop"))
        assertEquals(progress(step = null, hiddenUntil = null, missed = true), saved.of("python-gil"))
        assertEquals(progress(step = null, hiddenUntil = null, removed = true), saved.of("kotlin-data-class"))
    }

    @Test
    fun `an entry saved by an earlier version is read, and an entry that cannot be read is skipped without losing the others`() = runTest {
        dataStore.edit {
            it[stringPreferencesKey("question_progress_v1")] = listOf(
                "ladder=30;2026-12-06;-",
                "missed-and-removed=7;2026-11-06;mr",
                "only-missed=-;-;m",
                "no-equals-sign",
                "unknown-step=99;2026-11-06;-",
                "bad-date=7;yesterday;-",
                "too-few-parts=7;2026-11-06",
            ).joinToString(",")
        }

        val read = store.progress.first()
        assertEquals(progress(Step.DAYS_30, "2026-12-06"), read.of("ladder"))
        assertEquals(progress(Step.DAYS_7, "2026-11-06", missed = true, removed = true), read.of("missed-and-removed"))
        assertEquals(progress(step = null, hiddenUntil = null, missed = true), read.of("only-missed"))
        listOf("no-equals-sign", "unknown-step", "bad-date", "too-few-parts").forEach { id ->
            assertEquals("entry $id", progress(step = null, hiddenUntil = null), read.of(id))
        }

        store.knewIt("fresh")

        val saved = store.progress.first()
        assertEquals(progress(Step.DAYS_30, "2026-12-06"), saved.of("ladder"))
        assertEquals(progress(Step.DAYS_7, "2026-11-06", missed = true, removed = true), saved.of("missed-and-removed"))
        assertEquals(progress(Step.DAYS_7, "2026-11-06"), saved.of("fresh"))
    }

    @Test
    fun `a due date stays a calendar day when the clocks go forward in between`() = runTest {
        today("2026-03-20")
        store.knewIt("redis-event-loop")

        // 23:30 in Warsaw on 27 March. Thirty 24-hour days later it would already be 27 April local time.
        clock.instant = Instant.parse("2026-03-27T22:30:00Z")
        store.knewIt("redis-event-loop")

        assertEquals(progress(Step.DAYS_30, "2026-04-26"), store.progress.first().of("redis-event-loop"))
    }

    @Test
    fun `the day of an answer follows the phone's time zone at that moment`() = runTest {
        clock.instant = Instant.parse("2026-06-10T23:30:00Z")
        clock.zoneId = ZoneId.of("Europe/Warsaw")
        store.knewIt("redis-event-loop")

        clock.zoneId = ZoneId.of("America/New_York")
        store.knewIt("python-gil")

        val saved = store.progress.first()
        assertEquals(progress(Step.DAYS_7, "2026-06-18"), saved.of("redis-event-loop"))
        assertEquals(progress(Step.DAYS_7, "2026-06-17"), saved.of("python-gil"))
    }

    @Test
    fun `grades given at the same moment are all saved`() = runTest {
        val known = (1..10).map { "known-$it" }
        val missed = (1..10).map { "missed-$it" }

        coroutineScope {
            (known.map { id -> async { store.knewIt(id) } } + missed.map { id -> async { store.missed(id) } }).awaitAll()
        }

        val saved = store.progress.first()
        assertEquals(emptyList<String>(), known.filter { saved.of(it) != progress(Step.DAYS_7, "2026-11-06") })
        assertEquals(emptyList<String>(), missed.filter { saved.of(it) != progress(step = null, hiddenUntil = null, missed = true) })
    }

    @Test
    fun `tapping Knew it twice at the same moment climbs only one step`() = runTest {
        coroutineScope { List(2) { async { store.knewIt("redis-event-loop") } }.awaitAll() }

        assertEquals(progress(Step.DAYS_7, "2026-11-06"), store.progress.first().of("redis-event-loop"))
    }
}
