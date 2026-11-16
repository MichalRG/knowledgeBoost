package com.krzyzowski.knowledgeboost.ui.quote

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
class QuoteCalendarDates(val firstDay: LocalDate, val today: LocalDate) : SelectableDates {
    init {
        require(!firstDay.isAfter(today)) { "The quote calendar cannot start after today" }
    }

    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
        utcTimeMillis in millis(firstDay)..millis(today)

    override fun isSelectableYear(year: Int): Boolean = year in firstDay.year..today.year

    fun selection(day: LocalDate): LocalDate = day.coerceIn(firstDay, today)

    companion object {
        fun millis(day: LocalDate): Long = day.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        fun date(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
    }
}
