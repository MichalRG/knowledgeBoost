package com.krzyzowski.knowledgeboost.quote

import com.krzyzowski.knowledgeboost.content.DailyQuoteJson
import com.krzyzowski.knowledgeboost.content.DailyQuoteParser
import com.krzyzowski.knowledgeboost.content.requiredString
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeParseException

class DailyQuoteHistoryJson {
    fun encode(history: DailyQuoteHistory): String {
        val quotes = JSONArray()
        history.quotes.forEach { quotes.put(DailyQuoteJson.encode(it)) }
        val days = JSONArray()
        history.days.toSortedMap().forEach { (day, quote) ->
            days.put(JSONObject().put("day", day.toString()).put("quote", DailyQuoteJson.encode(quote)))
        }
        return JSONObject().put("quotes", quotes).put("days", days).toString()
    }

    fun decode(text: String): DailyQuoteHistory {
        try {
            val json = JSONObject(text)
            val quotes = DailyQuoteParser().parse(text)
            val entries = json.opt("days")
            require(entries is JSONArray && entries.length() > 0) { "Quote history: days must be a non-empty array" }
            val days = List(entries.length()) { index ->
                val where = "Quote history day ${index + 1}"
                val entry = entries.opt(index)
                require(entry is JSONObject) { "$where: must be an object" }
                LocalDate.parse(entry.requiredString(where, "day")) to DailyQuoteJson.decode(entry.opt("quote"), where)
            }
            require(days.map { it.first }.distinct().size == days.size) { "Quote history: duplicate dates" }
            return DailyQuoteHistory(days.toMap(), quotes)
        } catch (error: JSONException) {
            throw IllegalArgumentException("Quote history: invalid JSON (${error.message})", error)
        } catch (error: DateTimeParseException) {
            throw IllegalArgumentException("Quote history: invalid date (${error.message})", error)
        }
    }
}
