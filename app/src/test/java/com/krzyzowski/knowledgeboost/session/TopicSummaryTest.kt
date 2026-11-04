package com.krzyzowski.knowledgeboost.session

import com.krzyzowski.knowledgeboost.content.ContentBuilder.topic
import com.krzyzowski.knowledgeboost.schedule.LibraryProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class TopicSummaryTest {
    private val redis = topic("redis", listOf("new", "hidden-early", "missed", "removed", "hidden-and-missed"))

    private val progress = LibraryProgress.NONE
        .knewIt("hidden-early", LocalDate.parse("2026-11-02"))
        .missed("missed")
        .knewIt("removed", LocalDate.parse("2026-11-01"))
        .remove("removed")
        .missed("hidden-and-missed")
        .knewIt("hidden-and-missed", LocalDate.parse("2026-11-04"))

    @Test
    fun `a topic counts its available, hidden and missed questions and says when the next hidden one returns`() {
        val summary = TopicSummary.of(redis, progress, today = LocalDate.parse("2026-11-04"))

        assertEquals(2, summary.available)
        assertEquals(2, summary.hidden)
        assertEquals(2, summary.missed)
        // The removed question would return on 8 Nov, but a removed question counts nowhere.
        assertEquals(LocalDate.parse("2026-11-09"), summary.nextReturn)
    }

    @Test
    fun `a question counts as available again on the day it comes back and the next return moves on`() {
        val summary = TopicSummary.of(redis, progress, today = LocalDate.parse("2026-11-09"))

        assertEquals(3, summary.available)
        assertEquals(1, summary.hidden)
        assertEquals(LocalDate.parse("2026-11-11"), summary.nextReturn)
    }

    @Test
    fun `a topic with nothing hidden has no next return date`() {
        val summary = TopicSummary.of(redis, progress, today = LocalDate.parse("2026-12-01"))

        assertEquals(4, summary.available)
        assertEquals(0, summary.hidden)
        assertEquals(2, summary.missed)
        assertNull(summary.nextReturn)
    }
}
