package com.krzyzowski.knowledgeboost.content

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class DailyQuoteParser {
    fun parse(text: String): List<DailyQuote> {
        val json = try {
            JSONObject(text)
        } catch (error: JSONException) {
            throw IllegalArgumentException("$FILE_NAME: not a valid JSON object (${error.message})", error)
        }
        val quotes = json.opt("quotes")
        require(quotes is JSONArray && quotes.length() > 0) { "$FILE_NAME: quotes must be a non-empty array" }
        return List(quotes.length()) { index ->
            val where = "$FILE_NAME quote ${index + 1}"
            val item = quotes.opt(index)
            require(item is JSONObject) { "$where: must be an object" }
            val polish = item.opt("polish")
            require(polish == null || (polish is String && polish.isNotBlank())) {
                "$where: polish must be a non-blank string when supplied"
            }
            DailyQuote(
                text = item.requiredString(where, "text"),
                author = item.requiredString(where, "author"),
                source = item.requiredString(where, "source"),
                polish = polish,
            )
        }
    }

    companion object {
        const val FILE_NAME = "daily-quotes.json"
    }
}
