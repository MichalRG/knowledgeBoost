package com.krzyzowski.knowledgeboost.content

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class TopicParser {
    fun parse(fileName: String, text: String): Topic {
        val topicId = fileName.removeSuffix(".json")
        require(fileName.endsWith(".json") && SLUG.matches(topicId)) {
            "$fileName: the file name must be a lower-case slug ending in .json, such as kotlin-basics.json"
        }
        val json = try {
            JSONObject(text)
        } catch (e: JSONException) {
            throw IllegalArgumentException("$fileName: not a valid JSON object (${e.message})", e)
        }
        val items = json.opt("questions")
        require(items is JSONArray && items.length() > 0) { "$fileName: questions must be a non-empty array" }
        return Topic(
            id = topicId,
            category = json.requiredString(fileName, "category"),
            title = json.requiredString(fileName, "title"),
            questions = List(items.length()) { index ->
                question("$fileName question ${index + 1}", topicId, items.opt(index))
            },
        )
    }

    private fun question(where: String, topicId: String, item: Any?): Question {
        require(item is JSONObject) { "$where: must be an object" }
        val id = item.requiredString(where, "id")
        require(SLUG.matches(id)) { "$where: id $id must be a lower-case slug such as redis-event-loop" }
        val codeValue = item.opt("code")
        val code = (codeValue as? JSONArray)?.let { lines -> List(lines.length()) { lines.opt(it) } }
        require(codeValue == null || (code != null && code.isNotEmpty() && code.all { it is String })) {
            "$where: code must be a non-empty array of strings"
        }
        return Question(
            id = id,
            topicId = topicId,
            text = item.requiredString(where, "question"),
            answer = item.requiredString(where, "answer"),
            explanation = item.requiredString(where, "explanation"),
            code = code.orEmpty().filterIsInstance<String>(),
        )
    }

    private fun JSONObject.requiredString(where: String, key: String): String {
        val value = opt(key)
        require(value is String && value.isNotBlank()) { "$where: $key must be a non-blank string" }
        return value
    }

    private companion object {
        val SLUG = Regex("[a-z0-9]+(-[a-z0-9]+)*")
    }
}
