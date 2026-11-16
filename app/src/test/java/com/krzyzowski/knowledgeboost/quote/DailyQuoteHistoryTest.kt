package com.krzyzowski.knowledgeboost.quote

import com.krzyzowski.knowledgeboost.content.ContentBuilder
import com.krzyzowski.knowledgeboost.content.DailyQuoteParser
import com.krzyzowski.knowledgeboost.time.FakeClock
import com.krzyzowski.knowledgeboost.time.Today
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class DailyQuoteHistoryTest {
    private val start = LocalDate.parse("2026-11-05")
    private val original = listOf("First", "Second", "Third").map { ContentBuilder.quote(it) }
    private val replacement = listOf("Replacement one", "Replacement two").map { ContentBuilder.quote(it) }
    private val json = DailyQuoteHistoryJson()

    @Test
    fun `upgrade reconstructs every earlier date using all 31 supplied quotes for three complete cycles`() {
        val supplied = DailyQuoteParser().parse(File("src/main/assets/daily-quotes.json").readText())
        val history = DailyQuoteHistory().record(start, start.plusDays(92), supplied)

        assertEquals(93, history.days.size)
        assertEquals(start, history.firstDay)
        repeat(93) { offset -> assertEquals(supplied[offset % 31], history.days[start.plusDays(offset.toLong())]) }
        assertFalse(history.days.containsKey(start.minusDays(1)))
        assertFalse(history.days.containsKey(start.plusDays(93)))
    }

    @Test
    fun `same day edits preserve every field and the last edited list starts the following day without resetting the cycle`() {
        val saved = DailyQuoteHistory().record(start, start, original)
        val changed = saved.record(start, start, replacement).record(start, start, replacement.reversed())
        assertEquals(saved.days, changed.days)
        assertEquals(original[0], changed.days[start])

        val next = changed.record(start, start.plusDays(1), replacement.reversed())
        assertEquals(original[0], next.days[start])
        assertEquals(replacement[0], next.days[start.plusDays(1)])
    }

    @Test
    fun `skipped days including the return day use the last known list and a newly discovered list starts tomorrow`() {
        val initial = DailyQuoteHistory().record(start, start, original)
        val returned = initial.record(start, start.plusDays(5), replacement)

        repeat(6) { offset -> assertEquals(original[offset % 3], returned.days[start.plusDays(offset.toLong())]) }
        val next = returned.record(start, start.plusDays(6), replacement)
        assertEquals(replacement[0], next.days[start.plusDays(6)])
        assertEquals(returned.days, next.days.filterKeys { !it.isAfter(start.plusDays(5)) })
    }

    @Test
    fun `clock rollback preserves existing days and fills gaps even before the original start date`() {
        val initial = DailyQuoteHistory().record(start, start.plusDays(4), original)
        val rolledBack = initial.record(start, start.minusDays(4), replacement)
        val restored = json.decode(json.encode(rolledBack)).record(start, start.minusDays(1), replacement)

        assertEquals(initial.days, restored.days.filterKeys { !it.isBefore(start) })
        assertEquals(start.minusDays(4), restored.firstDay)
        repeat(9) { offset -> assertEquals(true, restored.days.containsKey(start.minusDays(4).plusDays(offset.toLong()))) }
        assertEquals(original[2], restored.days[start.minusDays(4)])
    }

    @Test
    fun `local midnight daylight saving and timezone changes select saved calendar days rather than elapsed hours`() {
        val clock = FakeClock(Instant.parse("2026-03-08T04:59:59Z"), ZoneId.of("America/New_York"))
        val today = Today(clock)
        val firstDay = today.date.value
        var history = DailyQuoteHistory().record(firstDay, firstDay, original)
        clock.instant = Instant.parse("2026-03-08T05:00:00Z")
        today.refresh()
        history = history.record(firstDay, today.date.value, original)
        assertEquals(original[1], history.days[today.date.value])

        clock.instant = Instant.parse("2026-03-09T04:00:00Z")
        today.refresh()
        history = history.record(firstDay, today.date.value, original)
        assertEquals(original[2], history.days[today.date.value])

        clock.zoneId = ZoneId.of("Pacific/Honolulu")
        today.refresh()
        assertEquals(original[1], history.days[today.date.value])
        assertEquals(history.days, history.record(firstDay, today.date.value, replacement).days)
    }

    @Test
    fun `JSON roundtrip preserves multiline text quoted sources optional translation and pending list changes`() {
        val quote = original.first().copy(text = "Line one\n\"Line two\"", source = "\"Source\", p. 12", polish = "\u017b\u00f3\u0142w")
        val history = DailyQuoteHistory().record(start, start.plusDays(2), listOf(quote, quote.copy(polish = null)))
            .record(start, start.plusDays(2), replacement)
        assertEquals(history, json.decode(json.encode(history)))
        assertEquals(json.encode(history), json.encode(json.decode(json.encode(history))))
    }

    @Test
    fun `malformed stored history and empty replacement lists fail explicitly`() {
        val valid = json.encode(DailyQuoteHistory().record(start, start, original))
        val duplicate = valid.replace("\"days\":[", "\"days\":[${org.json.JSONObject(valid).getJSONArray("days").get(0)},")
        listOf("{", "{}", valid.replace("2026-11-05", "not-a-date"), valid.replace("\"text\":", "\"missing\":"),
            valid.replace("\"days\":[", "\"days\":null,\"unused\":["), duplicate,
        ).forEach { invalid ->
            assertThrows("Expected invalid history: $invalid", IllegalArgumentException::class.java) { json.decode(invalid) }
        }
        assertThrows(IllegalArgumentException::class.java) { DailyQuoteHistory().record(start, start, emptyList()) }
    }
}
