package com.krzyzowski.knowledgeboost.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TopicParserTest {
    private val parser = TopicParser()

    private fun jsonObject(defaults: Map<String, String>, overrides: Array<out Pair<String, String?>>): String {
        val fields = LinkedHashMap(defaults)
        overrides.forEach { (key, value) -> if (value == null) fields.remove(key) else fields[key] = value }
        return fields.entries.joinToString(",", "{", "}") { (key, value) -> "\"$key\": $value" }
    }

    private fun question(vararg overrides: Pair<String, String?>): String = jsonObject(
        mapOf(
            "id" to "\"redis-event-loop\"",
            "question" to "\"Why does Redis use an event loop?\"",
            "answer" to "\"One thread serves many sockets.\"",
            "explanation" to "\"Commands never run in parallel.\"",
        ),
        overrides,
    )

    private fun topic(vararg overrides: Pair<String, String?>): String = jsonObject(
        mapOf("category" to "\"Databases\"", "title" to "\"Redis\"", "questions" to "[${question()}]"),
        overrides,
    )

    private fun parseFailure(fileName: String, text: String): String? = try {
        parser.parse(fileName, text)
        null
    } catch (e: IllegalArgumentException) {
        e.message.orEmpty()
    }

    private fun assertRejected(fileName: String, text: String, mention: String, rule: String) {
        val message = parseFailure(fileName, text)
        assertNotNull("accepted a file that $rule", message)
        assertTrue("$rule: message '$message' should name '$fileName'", message!!.contains(fileName))
        assertTrue("$rule: message '$message' should mention '$mention'", message.contains(mention))
    }

    @Test
    fun `a valid file becomes a topic with its questions in file order and its snippet lines`() {
        val text = topic(
            "questions" to listOf(
                question("code" to "[\"while (true) {\", \"    poll()\", \"}\"]", "difficulty" to "\"hard\""),
                question("id" to "\"redis-persistence\"", "question" to "\"How is data persisted?\""),
            ).joinToString(",", "[", "]"),
        )

        val parsed = parser.parse("redis-basics.json", text)

        assertEquals("redis-basics", parsed.id)
        assertEquals("Databases", parsed.category)
        assertEquals("Redis", parsed.title)
        assertEquals(listOf("redis-event-loop", "redis-persistence"), parsed.questions.map { it.id })
        val first = parsed.questions[0]
        assertEquals("redis-basics", first.topicId)
        assertEquals("Why does Redis use an event loop?", first.text)
        assertEquals("One thread serves many sockets.", first.answer)
        assertEquals("Commands never run in parallel.", first.explanation)
        assertEquals(listOf("while (true) {", "    poll()", "}"), first.code)
        assertEquals(emptyList<String>(), parsed.questions[1].code)
    }

    @Test
    fun `a file that breaks one content rule is rejected and the message names the file and the field`() {
        val first = { override: Pair<String, String?> -> topic("questions" to "[${question(override)}]") }
        val second = { override: Pair<String, String?> ->
            topic("questions" to "[${question()},${question("id" to "\"redis-second\"", override)}]")
        }
        val notAnArray = "question 2: code must be a non-empty array of strings"
        val cases = listOf(
            Triple("category is missing", topic("category" to null), "category must be a non-blank string"),
            Triple("title is blank", topic("title" to "\"  \""), "title must be a non-blank string"),
            Triple("questions is missing", topic("questions" to null), "questions must be a non-empty array"),
            Triple("questions is empty", topic("questions" to "[]"), "questions must be a non-empty array"),
            Triple("questions is an object", topic("questions" to "{}"), "questions must be a non-empty array"),
            Triple("a question is a string", topic("questions" to "[\"text\"]"), "question 1: must be an object"),
            Triple("a question has no id", first("id" to null), "question 1: id must be"),
            Triple("an id is blank", first("id" to "\"\""), "question 1: id must be"),
            Triple("an id is not a string", first("id" to "7"), "question 1: id must be"),
            Triple("a question text is blank", second("question" to "\" \""), "question 2: question must be"),
            Triple("an answer is blank", second("answer" to "\"   \""), "question 2: answer must be"),
            Triple("an answer is a number", second("answer" to "42"), "question 2: answer must be"),
            Triple("an explanation is missing", second("explanation" to null), "question 2: explanation must be"),
            Triple("code is a string", second("code" to "\"x = 1\""), notAnArray),
            Triple("code is an empty array", second("code" to "[]"), notAnArray),
            Triple("code has a number in it", second("code" to "[\"a\", 1]"), notAnArray),
            Triple("the text is not JSON", "this is not json", "not a valid JSON object"),
            Triple("the top level is an array", "[1, 2]", "not a valid JSON object"),
        )

        cases.forEach { (rule, text, mention) ->
            assertRejected("redis-basics.json", text, mention, "has this problem: $rule")
        }
    }

    @Test
    fun `an id that could break the saved progress is rejected`() {
        val ids = listOf(
            "redis,loop" to "comma",
            "redis=loop" to "equals sign",
            "redis;loop" to "semicolon",
            "redis|loop" to "pipe",
            "redis\\nloop" to "new line",
            "redis loop" to "space",
            "Redis-Loop" to "upper-case letter",
            "redis_loop" to "underscore",
            "-redis" to "leading hyphen",
            "redis--loop" to "double hyphen",
        )

        ids.forEach { (id, what) ->
            val text = topic("questions" to "[${question("id" to "\"$id\"")}]")
            assertRejected("redis-basics.json", text, "must be a lower-case slug", "uses an id with a $what")
        }
    }

    @Test
    fun `a file name that is not a lower-case slug ending in json is rejected`() {
        val names = listOf("Redis.json", "redis.txt", "redis", "redis basics.json", "redis_basics.json", ".json")

        names.forEach { name -> assertRejected(name, topic(), "file name must be", "is named '$name'") }
    }
}
