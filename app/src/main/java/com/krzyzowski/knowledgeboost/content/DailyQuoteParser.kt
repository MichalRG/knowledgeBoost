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
            DailyQuoteJson.decode(quotes.opt(index), "$FILE_NAME quote ${index + 1}")
        }
    }

    companion object {
        const val FILE_NAME = "daily-quotes.json"
    }
}
