package com.krzyzowski.knowledgeboost.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class TopicLibraryMergerTest {
    private val bundled = ContentBuilder.library(ContentBuilder.topic("bundled", listOf("known"), category = "Built in"))
    private val merger = TopicLibraryMerger()

    @Test
    fun `folder topics add categories and preserve the bundled questions and code lines`() {
        val library = merger.merge(bundled, listOf(TopicFileBuilder.file()))

        assertEquals(listOf("Built in", "Programming"), library.categories)
        assertEquals(bundled.topic("bundled"), library.topic("bundled"))
        assertEquals(listOf("known", "added-question"), library.questions.map { it.id })
        assertEquals(listOf("first", "", "last"), library.questions.last().code)
    }

    @Test
    fun `an empty folder keeps all bundled topics`() {
        assertEquals(bundled.topics, merger.merge(bundled, emptyList()).topics)
    }

    @Test
    fun `duplicate topic filenames cannot override bundled or other folder topics`() {
        listOf(
            listOf(TopicFileBuilder.file("bundled", id = "new-question")),
            listOf(TopicFileBuilder.file(), TopicFileBuilder.file()),
        ).forEach { files ->
            val error = assertThrows(IllegalArgumentException::class.java) { merger.merge(bundled, files) }
            assertTrue(error.message.orEmpty().contains("topic id already exists"))
        }
    }

    @Test
    fun `duplicate question ids are rejected across bundled and folder topics and within a file`() {
        val insideOneFile = TopicFile(
            "repeated.json",
            """{"category":"Programming","title":"Repeated","questions":[
                {"id":"duplicate","question":"First?","answer":"A","explanation":"E"},
                {"id":"duplicate","question":"Second?","answer":"A","explanation":"E"}
            ]}""",
        )
        val conflicts = listOf(
            listOf(TopicFileBuilder.file(id = "known")),
            listOf(TopicFileBuilder.file("one", "duplicate"), TopicFileBuilder.file("two", "duplicate")),
            listOf(insideOneFile),
        )
        conflicts.forEach { files ->
            val error = assertThrows(IllegalArgumentException::class.java) { merger.merge(bundled, files) }
            assertTrue(error.message.orEmpty().contains("question id"))
        }
    }

    @Test
    fun `invalid JSON and invalid filenames stop the whole folder import`() {
        listOf(TopicFile("broken.json", "{"), TopicFileBuilder.file().copy(name = "Added.JSON")).forEach { invalid ->
            val error = assertThrows(IllegalArgumentException::class.java) {
                merger.merge(bundled, listOf(TopicFileBuilder.file("valid"), invalid))
            }
            assertTrue(error.message.orEmpty().contains(invalid.name))
        }
    }

    @Test
    fun `question order follows the JSON rather than ids`() {
        val file = TopicFile(
            "order.json",
            """{"category":"Programming","title":"Order","questions":[
                {"id":"z-last","question":"Z?","answer":"Z","explanation":"Z"},
                {"id":"a-first","question":"A?","answer":"A","explanation":"A"}
            ]}""",
        )

        assertEquals(listOf("z-last", "a-first"), merger.merge(bundled, listOf(file)).topic("order")?.questions?.map { it.id })
    }
}
