package com.krzyzowski.knowledgeboost.schedule

import java.time.LocalDate

data class QuestionProgress(val step: Step?, val hiddenUntil: LocalDate?, val missed: Boolean, val removed: Boolean) {
    val nextStep: Step
        get() = step?.next ?: Step.DAYS_7

    fun isHiddenOn(day: LocalDate): Boolean = hiddenUntil != null && hiddenUntil.isAfter(day)

    fun knewIt(day: LocalDate): QuestionProgress {
        if (isHiddenOn(day)) return this
        return copy(step = nextStep, hiddenUntil = day.plusDays(nextStep.days.toLong()))
    }

    fun markedMissed(): QuestionProgress = copy(step = null, hiddenUntil = null, missed = true)

    companion object {
        val NEW = QuestionProgress(step = null, hiddenUntil = null, missed = false, removed = false)
    }
}
