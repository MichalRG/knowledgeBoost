package com.krzyzowski.knowledgeboost.content

import com.krzyzowski.knowledgeboost.schedule.LibraryProgress
import com.krzyzowski.knowledgeboost.schedule.QuestionProgress
import com.krzyzowski.knowledgeboost.schedule.Step
import com.krzyzowski.knowledgeboost.session.ResumePositions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.LocalDate

class BundledTopicsTest {
    private val topicFiles = File("src/main/assets/topics").listFiles().orEmpty().sortedBy { it.name }

    private fun bundledTopics(): List<Topic> = topicFiles.map { TopicParser().parse(it.name, it.readText()) }

    @Test
    fun `every bundled topic file parses and has all its required fields`() {
        assertTrue("no topic files were found in src/main/assets/topics", topicFiles.isNotEmpty())

        bundledTopics().forEach { topic ->
            assertTrue("${topic.id}: category is blank", topic.category.isNotBlank())
            assertTrue("${topic.id}: title is blank", topic.title.isNotBlank())
            assertTrue("${topic.id}: has no questions", topic.questions.isNotEmpty())
            topic.questions.forEach { question ->
                assertTrue("${question.id}: question text is blank", question.text.isNotBlank())
                assertTrue("${question.id}: answer is blank", question.answer.isNotBlank())
                assertTrue("${question.id}: explanation is blank", question.explanation.isNotBlank())
                assertEquals("${question.id}: belongs to the topic of its file", topic.id, question.topicId)
            }
        }
    }

    @Test
    fun `question ids are unique across all bundled files`() {
        val ids = bundledTopics().flatMap { topic -> topic.questions.map { it.id } }

        val usedTwice = ids.groupBy { it }.filterValues { it.size > 1 }.keys.toList()
        assertEquals("ids used more than once", emptyList<String>(), usedTwice)
    }

    @Test
    fun `every bundled question id and topic id survives saving and reading back`() {
        val topics = bundledTopics()
        val ids = topics.flatMap { topic -> topic.questions.map { it.id } }

        val day = LocalDate.parse("2026-11-04")
        val reloaded = LibraryProgress.decode(ids.fold(LibraryProgress.NONE) { saved, id -> saved.knewIt(id, day) }.encode())
        val knewItToday = QuestionProgress(Step.DAYS_7, LocalDate.parse("2026-11-11"), missed = false, removed = false)
        assertEquals("ids that did not come back intact", emptyList<String>(), ids.filter { reloaded.of(it) != knewItToday })

        val lastIds = topics.map { it.questions.last().id }
        val stopped = topics.zip(lastIds).fold(ResumePositions.NONE) { positions, (topic, id) -> positions.with(topic.id, id) }
        val resumeReloaded = ResumePositions.decode(stopped.encode())
        assertEquals(lastIds, topics.map { resumeReloaded.of(it.id) })
    }

    @Test
    fun `the bundled library contains the supplied programming topics instead of the samples`() {
        val topics = bundledTopics()

        assertEquals(setOf("Programming"), topics.map { it.category }.toSet())
        assertEquals(
            mapOf(
                "concurrency" to 38,
                "deadlocks" to 29,
                "python-gil-concurrency-debugging" to 22,
            ),
            topics.associate { it.id to it.questions.size },
        )
        assertEquals(
            listOf("Concurrency", "Deadlocks", "Python GIL and Concurrency Debugging"),
            topics.map { it.title },
        )
    }

    @Test
    fun `every supplied topic includes code examples`() {
        assertEquals(
            emptyList<String>(),
            bundledTopics().filter { topic -> topic.questions.none { it.code.isNotEmpty() } }.map { it.id },
        )
    }
}
