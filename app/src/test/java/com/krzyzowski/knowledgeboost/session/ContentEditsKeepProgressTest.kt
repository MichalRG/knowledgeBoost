package com.krzyzowski.knowledgeboost.session

import com.krzyzowski.knowledgeboost.content.ContentBuilder.library
import com.krzyzowski.knowledgeboost.content.ContentBuilder.topic
import com.krzyzowski.knowledgeboost.schedule.LibraryProgress
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ContentEditsKeepProgressTest {
    private val today = LocalDate.parse("2026-11-04")
    private val hiddenUntil = LocalDate.parse("2026-11-11")

    // q1 is hidden for 7 days, q2 is missed, q3 is removed; read back from the saved text like after a restart.
    private fun savedProgress(): LibraryProgress {
        val before = LibraryProgress.NONE.knewIt("q1", today).missed("q2").remove("q3")
        return LibraryProgress.decode(before.encode())
    }

    @Test
    fun `editing the text and reordering the questions keeps every question's state`() {
        val progress = savedProgress()
        val original = topic("redis", listOf("q1", "q2", "q3", "q4"))
        val reordered = original.copy(
            questions = original.questions.reversed().map { if (it.id == "q2") it.copy(text = "Reworded question") else it },
        )
        val content = library(reordered)

        assertEquals(listOf("q4", "q2"), CardQueue.available(reordered, content, progress, today).cards.map { it.id })
        assertEquals(
            TopicSummary(available = 2, hidden = 1, missed = 1, nextReturn = hiddenUntil),
            TopicSummary.of(reordered, progress, today),
        )
        assertEquals(listOf("q2"), CardQueue.missed(content, progress).cards.map { it.id })
        assertEquals(listOf("q3"), CardQueue.removed(content, progress).cards.map { it.id })
    }

    @Test
    fun `a deleted question disappears and comes back with its state when the same id is added again`() {
        val shortened = topic("redis", listOf("q3", "q4"))
        val readded = topic("redis", listOf("q1", "q3", "q4", "q2"))
        val progress = savedProgress()

        assertEquals(
            TopicSummary(available = 1, hidden = 0, missed = 0, nextReturn = null),
            TopicSummary.of(shortened, progress, today),
        )
        assertEquals(emptyList<String>(), CardQueue.missed(library(shortened), progress).cards.map { it.id })

        val gradedMeanwhile = progress.knewIt("q4", today)
        assertEquals(
            TopicSummary(available = 1, hidden = 2, missed = 1, nextReturn = hiddenUntil),
            TopicSummary.of(readded, gradedMeanwhile, today),
        )
        assertEquals(listOf("q2"), CardQueue.missed(library(readded), gradedMeanwhile).cards.map { it.id })
    }
}
