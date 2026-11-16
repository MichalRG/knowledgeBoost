package com.krzyzowski.knowledgeboost.ui.quote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.util.TimeZone

class QuoteCalendarDatesTest {
    private val first = LocalDate.parse("2026-11-05")
    private val today = first.plusDays(4)
    private val dates = QuoteCalendarDates(first, today)

    @Test
    fun `calendar enables the first day every past day and today but not earlier or future dates`() {
        repeat(5) { offset -> assertTrue(dates.isSelectableDate(QuoteCalendarDates.millis(first.plusDays(offset.toLong())))) }
        assertFalse(dates.isSelectableDate(QuoteCalendarDates.millis(first.minusDays(1))))
        assertFalse(dates.isSelectableDate(QuoteCalendarDates.millis(today.plusDays(1))))
        assertFalse(dates.isSelectableYear(2025))
        assertTrue(dates.isSelectableYear(2026))
        assertFalse(dates.isSelectableYear(2027))
    }

    @Test
    fun `date picker conversions use UTC in all device timezones including daylight saving boundaries`() {
        val original = TimeZone.getDefault()
        try {
            listOf("Pacific/Kiritimati", "America/Los_Angeles", "Europe/Warsaw").forEach { zone ->
                TimeZone.setDefault(TimeZone.getTimeZone(zone))
                listOf("2026-03-08", "2026-11-01", "2026-11-05").forEach { text ->
                    val day = LocalDate.parse(text)
                    val expected = Instant.parse("${text}T00:00:00Z").toEpochMilli()
                    assertEquals(zone, expected, QuoteCalendarDates.millis(day))
                    assertEquals(zone, day, QuoteCalendarDates.date(expected))
                }
            }
        } finally {
            TimeZone.setDefault(original)
        }
    }

    @Test
    fun `restored selection stays in range after clock rollback and today remains available for a one-day calendar`() {
        assertEquals(first, dates.selection(first.minusDays(10)))
        assertEquals(today, dates.selection(today.plusDays(10)))
        assertEquals(first.plusDays(1), dates.selection(first.plusDays(1)))
        val oneDay = QuoteCalendarDates(today, today)
        assertTrue(oneDay.isSelectableDate(QuoteCalendarDates.millis(today)))
        assertFalse(oneDay.isSelectableDate(QuoteCalendarDates.millis(today.minusDays(1))))
        assertThrows(IllegalArgumentException::class.java) { QuoteCalendarDates(today, first) }
    }
}
