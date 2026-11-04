package com.krzyzowski.knowledgeboost.ui

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateLabel {
    private val FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)

    fun of(date: LocalDate): String = FORMAT.format(date)
}
