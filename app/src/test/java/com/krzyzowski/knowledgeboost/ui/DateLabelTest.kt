package com.krzyzowski.knowledgeboost.ui

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class DateLabelTest {
    private val phoneLanguage = Locale.getDefault()

    @After
    fun restoreLanguage() {
        Locale.setDefault(phoneLanguage)
    }

    @Test
    fun `a date reads like 5 Mar 2026 in English, whatever language the phone uses`() {
        Locale.setDefault(Locale.GERMAN)

        assertEquals("5 Mar 2026", DateLabel.of(LocalDate.parse("2026-03-05")))
        assertEquals("11 Nov 2026", DateLabel.of(LocalDate.parse("2026-11-11")))
    }
}
