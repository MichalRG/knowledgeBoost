package com.krzyzowski.knowledgeboost.content

import org.json.JSONObject

internal object DailyQuoteJson {
    fun decode(value: Any?, where: String): DailyQuote {
        require(value is JSONObject) { "$where: must be an object" }
        val polish = value.opt("polish")
        require(polish == null || (polish is String && polish.isNotBlank())) {
            "$where: polish must be a non-blank string when supplied"
        }
        return DailyQuote(
            text = value.requiredString(where, "text"),
            author = value.requiredString(where, "author"),
            source = value.requiredString(where, "source"),
            polish = polish,
        )
    }

    fun encode(quote: DailyQuote): JSONObject = JSONObject()
        .put("text", quote.text)
        .put("author", quote.author)
        .put("source", quote.source)
        .put("polish", quote.polish)
}
