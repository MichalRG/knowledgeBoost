package com.krzyzowski.knowledgeboost.content

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DailyQuoteParserTest {
    private val parser = DailyQuoteParser()

    @Test
    fun `the 31 supplied quotes keep their order and have authors sources and Polish translations`() {
        val quotes = parser.parse(File("src/main/assets/daily-quotes.json").readText())

        assertEquals(31, quotes.size)
        assertEquals(31, quotes.map { it.text }.toSet().size)
        assertEquals("Richard Feynman", quotes.first().author)
        assertEquals("Cargo Cult Science (1974). Wikiquote.", quotes.first().source)
        assertEquals("Toni Morrison", quotes.last().author)
        assertTrue(quotes.all { it.text.isNotBlank() && it.author.isNotBlank() && it.source.isNotBlank() })
        assertTrue(quotes.all { !it.polish.isNullOrBlank() })
    }

    @Test
    fun `folder quotes preserve file order and do not require a translation`() {
        val quotes = parser.parse(ContentBuilder.quoteFile("Second alphabetically", "First alphabetically").text)

        assertEquals(listOf("Second alphabetically", "First alphabetically"), quotes.map { it.text })
        assertNull(quotes.first().polish)
    }

    @Test
    fun `invalid JSON and missing empty or invalid quote lists report the reserved filename`() {
        listOf("{", "[]", "{}", """{"quotes":[]}""", """{"quotes":null}""", """{"quotes":[5]}""").forEach { text ->
            val error = assertThrows(IllegalArgumentException::class.java) { parser.parse(text) }
            assertTrue(error.message.orEmpty().contains(DailyQuoteParser.FILE_NAME))
        }
    }

    @Test
    fun `text author and source must each be a nonblank string`() {
        listOf("text", "author", "source").forEach { field ->
            listOf(null, JSONObject.NULL, "", " ", 42).forEach { value ->
                val quote = JSONObject(ContentBuilder.quoteFile("Thought").text).getJSONArray("quotes").getJSONObject(0)
                if (value == null) quote.remove(field) else quote.put(field, value)
                val text = JSONObject().put("quotes", JSONArray().put(quote)).toString()

                val error = assertThrows(IllegalArgumentException::class.java) { parser.parse(text) }

                assertTrue(error.message.orEmpty().contains("quote 1: $field"))
            }
        }
    }

    @Test
    fun `a supplied Polish translation must be a nonblank string`() {
        listOf(JSONObject.NULL, "", " ", 42).forEach { value ->
            val quote = JSONObject().put("text", "Thought").put("author", "Author").put("source", "Source").put("polish", value)
            val text = JSONObject().put("quotes", JSONArray().put(quote)).toString()

            val error = assertThrows(IllegalArgumentException::class.java) { parser.parse(text) }

            assertTrue(error.message.orEmpty().contains("polish"))
        }
    }
}
