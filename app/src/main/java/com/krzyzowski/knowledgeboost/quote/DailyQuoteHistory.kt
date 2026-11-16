package com.krzyzowski.knowledgeboost.quote

import com.krzyzowski.knowledgeboost.content.DailyQuote
import com.krzyzowski.knowledgeboost.time.DailyQuoteCycle
import java.time.LocalDate

data class DailyQuoteHistory(
    val days: Map<LocalDate, DailyQuote> = emptyMap(),
    val quotes: List<DailyQuote> = emptyList(),
) {
    val firstDay: LocalDate? get() = days.keys.minOrNull()

    fun record(startedOn: LocalDate, today: LocalDate, newQuotes: List<DailyQuote>): DailyQuoteHistory {
        require(newQuotes.isNotEmpty()) { "Quote history requires a non-empty quote list" }
        val saved = days.toMutableMap()
        val cycle = DailyQuoteCycle()
        // The last loaded list assigns skipped days and today; a new list starts tomorrow.
        val schedule = quotes.ifEmpty { newQuotes }
        var day = minOf(startedOn, today, firstDay ?: startedOn)
        while (!day.isAfter(today)) {
            if (day !in saved) saved[day] = cycle.onDay(schedule, startedOn, day)
            day = day.plusDays(1)
        }
        return DailyQuoteHistory(saved.toMap(), newQuotes.toList())
    }
}
