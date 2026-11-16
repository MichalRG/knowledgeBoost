package com.krzyzowski.knowledgeboost.quote

data class QuoteHistoryState(
    val history: DailyQuoteHistory? = null,
    val error: String? = null,
)
