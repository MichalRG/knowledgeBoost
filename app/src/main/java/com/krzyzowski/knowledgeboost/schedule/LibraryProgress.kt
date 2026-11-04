package com.krzyzowski.knowledgeboost.schedule

import java.time.LocalDate

class LibraryProgress private constructor(private val questions: Map<String, QuestionProgress>) {
    fun of(id: String): QuestionProgress = questions[id] ?: QuestionProgress.NEW

    fun knewIt(id: String, day: LocalDate): LibraryProgress = updated(id, of(id).knewIt(day))

    fun missed(id: String): LibraryProgress = updated(id, of(id).markedMissed())

    fun remove(id: String): LibraryProgress = updated(id, of(id).copy(removed = true))

    fun restore(id: String): LibraryProgress = updated(id, of(id).copy(removed = false))

    fun clearMissed(id: String): LibraryProgress = updated(id, of(id).copy(missed = false))

    private fun updated(id: String, progress: QuestionProgress) = LibraryProgress(questions + (id to progress))

    fun encode(): String = questions.entries
        .sortedBy { it.key }
        .joinToString(",") { (id, progress) ->
            val flags = buildString {
                if (progress.missed) append(MISSED)
                if (progress.removed) append(REMOVED)
            }
            "$id=${progress.step?.code ?: UNSET};${progress.hiddenUntil ?: UNSET};${flags.ifEmpty { UNSET }}"
        }

    companion object {
        private const val UNSET = "-"
        private const val MISSED = 'm'
        private const val REMOVED = 'r'

        val NONE = LibraryProgress(emptyMap())

        fun decode(encoded: String): LibraryProgress = LibraryProgress(
            encoded.split(',')
                .filter { it.isNotBlank() }
                .mapNotNull { entry ->
                    runCatching {
                        val (id, value) = entry.split('=', limit = 2)
                        val (step, hiddenUntil, flags) = value.split(';')
                        id to QuestionProgress(
                            step = step.takeUnless { it == UNSET }?.let(Step::fromCode),
                            hiddenUntil = hiddenUntil.takeUnless { it == UNSET }?.let(LocalDate::parse),
                            missed = MISSED in flags,
                            removed = REMOVED in flags,
                        )
                    }.getOrNull()
                }
                .toMap(),
        )
    }
}
