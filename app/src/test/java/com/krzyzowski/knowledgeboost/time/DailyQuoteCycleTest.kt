package com.krzyzowski.knowledgeboost.time

import com.krzyzowski.knowledgeboost.content.ContentBuilder
import com.krzyzowski.knowledgeboost.content.DailyQuoteParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class DailyQuoteCycleTest {
    private val cycle = DailyQuoteCycle()
    private val start = LocalDate.parse("2026-11-05")
    private val quotes = listOf("First", "Second", "Third").map { ContentBuilder.quote(it) }

    @Test
    fun `the first day shows the first quote and reopening on the same day does not advance it`() {
        repeat(5) { assertSame(quotes.first(), cycle.onDay(quotes, start, start)) }
    }

    @Test
    fun `every one of the 31 supplied thoughts appears in order and all three full cycles wrap correctly`() {
        val supplied = DailyQuoteParser().parse(File("src/main/assets/daily-quotes.json").readText())
        assertEquals(31, supplied.size)

        repeat(93) { day ->
            assertEquals("day $day", supplied[day % 31], cycle.onDay(supplied, start, start.plusDays(day.toLong())))
        }
    }

    @Test
    fun `calendar days advance even if the app was closed for several days`() {
        assertEquals(quotes[1], cycle.onDay(quotes, start, start.plusDays(4)))
        assertEquals(quotes[2], cycle.onDay(quotes, start, start.plusDays(1001)))
    }

    @Test
    fun `local midnight advances the quote and a 23 hour daylight saving day still counts as one day`() {
        val clock = FakeClock(Instant.parse("2026-03-08T04:59:59Z"), ZoneId.of("America/New_York"))
        val today = Today(clock)
        val began = today.date.value
        assertEquals(quotes[0], cycle.onDay(quotes, began, today.date.value))

        clock.instant = Instant.parse("2026-03-08T05:00:00Z")
        today.refresh()
        assertEquals(quotes[1], cycle.onDay(quotes, began, today.date.value))
        clock.instant = Instant.parse("2026-03-08T08:30:00Z")
        today.refresh()
        assertEquals(quotes[1], cycle.onDay(quotes, began, today.date.value))
        clock.instant = Instant.parse("2026-03-09T04:00:00Z")
        today.refresh()
        assertEquals(quotes[2], cycle.onDay(quotes, began, today.date.value))
    }

    @Test
    fun `a changed device timezone follows its local calendar date`() {
        val clock = FakeClock(Instant.parse("2026-11-05T23:30:00Z"), ZoneId.of("UTC"))
        val today = Today(clock)
        assertEquals(quotes[0], cycle.onDay(quotes, start, today.date.value))

        clock.zoneId = ZoneId.of("Europe/Warsaw")
        today.refresh()

        assertEquals(quotes[1], cycle.onDay(quotes, start, today.date.value))
    }

    @Test
    fun `single quote lists date rollback and list resizing remain deterministic`() {
        assertEquals(quotes[2], cycle.onDay(quotes, start, start.minusDays(1)))
        assertEquals(quotes[0], cycle.onDay(quotes.take(1), start, start.plusDays(1000)))
        assertEquals(quotes[1], cycle.onDay(quotes.take(2), start, start.plusDays(3)))
        assertThrows(IllegalArgumentException::class.java) { cycle.onDay(emptyList(), start, start) }
    }
}
