package com.krzyzowski.knowledgeboost.time

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class TodayTest {
    @Test
    fun `the shared date stays on the day it was read until it is refreshed, then follows the clock`() {
        val clock = FakeClock(Instant.parse("2026-11-04T10:00:00Z"), ZoneId.of("Europe/Warsaw"))
        val today = Today(clock)

        clock.instant = Instant.parse("2026-11-06T10:00:00Z")
        assertEquals(LocalDate.parse("2026-11-04"), today.date.value)

        today.refresh()
        assertEquals(LocalDate.parse("2026-11-06"), today.date.value)
    }
}
