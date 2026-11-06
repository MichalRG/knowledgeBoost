package com.krzyzowski.knowledgeboost.time

import com.krzyzowski.knowledgeboost.content.DailyQuote
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class DailyQuoteCycle {
    fun onDay(quotes: List<DailyQuote>, startedOn: LocalDate, today: LocalDate): DailyQuote {
        require(quotes.isNotEmpty()) { "The daily thoughts list must not be empty." }
        val days = ChronoUnit.DAYS.between(startedOn, today)
        return quotes[Math.floorMod(days, quotes.size.toLong()).toInt()]
    }
}
