package com.krzyzowski.knowledgeboost.content

import com.krzyzowski.knowledgeboost.content.ContentBuilder.library
import com.krzyzowski.knowledgeboost.content.ContentBuilder.topic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LibraryTest {
    @Test
    fun `categories and topics are listed A to Z whatever the file order, and questions keep file order`() {
        val library = library(
            topic("python", listOf("py-2", "py-1"), category = "Programming", title = "Python"),
            topic("redis", listOf("redis-1"), category = "databases", title = "Redis"),
            topic("kotlin", listOf("kt-1", "kt-3", "kt-2"), category = "Programming", title = "kotlin"),
            topic("aws", listOf("aws-1"), category = "Cloud", title = "AWS"),
            topic("mongo", listOf("mongo-1"), category = "databases", title = "MongoDB"),
        )

        assertEquals(listOf("Cloud", "databases", "Programming"), library.categories)
        assertEquals(listOf("mongo", "redis"), library.topicsIn("databases").map { it.id })
        assertEquals(listOf("kotlin", "python"), library.topicsIn("Programming").map { it.id })
        assertEquals(
            listOf("aws-1", "mongo-1", "redis-1", "kt-1", "kt-3", "kt-2", "py-2", "py-1"),
            library.questions.map { it.id },
        )
        assertEquals(listOf(0, 1, 2, 3, 4, 5, 6, 7), library.questions.map { library.orderOf(it.id) })
    }

    @Test
    fun `topics in a category are listed by title, not by file name`() {
        val library = library(
            topic("a-last-file", listOf("q-1"), category = "Programming", title = "Zig"),
            topic("z-first-file", listOf("q-2"), category = "Programming", title = "Ada"),
        )

        assertEquals(listOf("z-first-file", "a-last-file"), library.topicsIn("Programming").map { it.id })
        assertEquals(listOf("q-2", "q-1"), library.questions.map { it.id })
    }

    @Test
    fun `a question can be traced back to its category and topic, and a deleted one is unknown`() {
        val library = library(topic("redis", listOf("redis-1"), category = "Databases", title = "Redis"))

        val owner = library.topicOf(library.questions.single())

        assertEquals("Databases", owner.category)
        assertEquals("Redis", owner.title)
        assertEquals("redis", library.topic("redis")?.id)
        assertNull(library.topic("kotlin"))
        assertNull(library.orderOf("kotlin-1"))
    }
}
