package com.krzyzowski.knowledgeboost.session

import com.krzyzowski.knowledgeboost.content.ContentBuilder.library
import com.krzyzowski.knowledgeboost.content.ContentBuilder.topic
import com.krzyzowski.knowledgeboost.schedule.LibraryProgress
import com.krzyzowski.knowledgeboost.schedule.Step
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class CardQueueTest {
    private val today = LocalDate.parse("2026-11-04")
    private val redis = topic("redis", listOf("r1", "r2", "r3", "r4"), category = "Databases", title = "Redis")
    private val kotlin = topic("kotlin", listOf("k1", "k2", "k3"), category = "Programming", title = "Kotlin")
    private val aws = topic("aws", listOf("a1", "a2"), category = "Cloud", title = "AWS")
    private val content = library(kotlin, redis, aws)

    private fun availableRedis(progress: LibraryProgress) = CardQueue.available(redis, content, progress, today)

    @Test
    fun `a topic queue lists its available questions in file order without hidden or removed ones`() {
        val progress = LibraryProgress.NONE
            .knewIt("r1", LocalDate.parse("2026-11-01"))
            .knewIt("r2", LocalDate.parse("2026-10-28"))
            .remove("r3")
            .missed("r4")

        val queue = availableRedis(progress)

        // r1 comes back on 8 Nov, r2 returned today (4 Nov).
        assertEquals(listOf("r2", "r4"), queue.cards.map { it.id })
        assertEquals(2, queue.size)
    }

    @Test
    fun `resume lands on the stopped question when it is still available`() {
        val queue = availableRedis(LibraryProgress.NONE.knewIt("r2", today))

        assertEquals(2, queue.positionOf("r4"))
        assertEquals(0, queue.positionOf("r1"))
    }

    @Test
    fun `resume goes to the next available question in file order when the stopped one is hidden`() {
        val queue = availableRedis(LibraryProgress.NONE.knewIt("r2", today).knewIt("r3", today))

        assertEquals(listOf("r1", "r4"), queue.cards.map { it.id })
        assertEquals(1, queue.positionOf("r2"))
        assertEquals(1, queue.positionOf("r3"))
    }

    @Test
    fun `resume goes to the first question when nothing follows the stopped one, nothing was stored or the id is gone`() {
        val queue = availableRedis(LibraryProgress.NONE.knewIt("r4", today))

        assertEquals(listOf("r1", "r2", "r3"), queue.cards.map { it.id })
        assertEquals(0, queue.positionOf("r4"))
        assertEquals(0, queue.positionOf(null))
        assertEquals(0, queue.positionOf("deleted-question"))
    }

    @Test
    fun `after any grade the next card is the following one and the last one wraps to the first`() {
        val queue = availableRedis(LibraryProgress.NONE)
        val onlyR1Left = availableRedis(LibraryProgress.NONE.knewIt("r2", today).knewIt("r3", today).knewIt("r4", today))

        assertEquals("r2", queue.after(0))
        assertEquals("r4", queue.after(2))
        assertEquals("r1", queue.after(3))
        assertEquals("r1", onlyR1Left.after(0))
    }

    @Test
    fun `the Missed queue spans topics in library order, includes hidden questions, and leaves out removed and deleted ones`() {
        val progress = LibraryProgress.NONE
            .missed("k2")
            .missed("r1")
            .knewIt("r1", today)
            .missed("a1")
            .remove("a1")
            .missed("ghost")
            .knewIt("r3", today)

        val queue = CardQueue.missed(content, progress)

        assertEquals(listOf("r1", "k2"), queue.cards.map { it.id })
        assertEquals("Databases", queue.topicAt(0).category)
        assertEquals("Redis", queue.topicAt(0).title)
        assertEquals("Programming", queue.topicAt(1).category)
        assertEquals("Kotlin", queue.topicAt(1).title)
        assertEquals(Step.DAYS_7, queue.progressAt(0).step)
        assertEquals(LocalDate.parse("2026-11-11"), queue.progressAt(0).hiddenUntil)
    }

    @Test
    fun `the Removed queue lists removed questions in library order, hidden or missed or not`() {
        val progress = LibraryProgress.NONE
            .remove("k3")
            .remove("r2")
            .missed("r2")
            .knewIt("a2", today)
            .remove("a2")
            .remove("ghost")
            .missed("k1")

        val queue = CardQueue.removed(content, progress)

        assertEquals(listOf("a2", "r2", "k3"), queue.cards.map { it.id })
    }
}
