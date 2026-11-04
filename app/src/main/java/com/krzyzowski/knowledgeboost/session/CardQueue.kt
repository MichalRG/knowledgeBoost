package com.krzyzowski.knowledgeboost.session

import com.krzyzowski.knowledgeboost.content.Library
import com.krzyzowski.knowledgeboost.content.Question
import com.krzyzowski.knowledgeboost.content.Topic
import com.krzyzowski.knowledgeboost.schedule.LibraryProgress
import com.krzyzowski.knowledgeboost.schedule.QuestionProgress
import java.time.LocalDate

class CardQueue private constructor(
    val cards: List<Question>,
    private val library: Library,
    private val progress: LibraryProgress,
) {
    val size: Int
        get() = cards.size

    fun at(position: Int): Question = cards[position]

    fun progressAt(position: Int): QuestionProgress = progress.of(cards[position].id)

    fun topicAt(position: Int): Topic = library.topicOf(cards[position])

    fun positionOf(id: String?): Int {
        val exact = cards.indexOfFirst { it.id == id }
        if (exact >= 0) return exact
        val order = id?.let(library::orderOf) ?: return 0
        return cards.indexOfFirst { (library.orderOf(it.id) ?: 0) > order }.coerceAtLeast(0)
    }

    fun after(position: Int): String = cards[(position + 1) % size].id

    companion object {
        fun available(topic: Topic, library: Library, progress: LibraryProgress, today: LocalDate) = CardQueue(
            topic.questions.filter { question ->
                progress.of(question.id).let { !it.removed && !it.isHiddenOn(today) }
            },
            library,
            progress,
        )

        fun missed(library: Library, progress: LibraryProgress) = CardQueue(
            library.questions.filter { question -> progress.of(question.id).let { it.missed && !it.removed } },
            library,
            progress,
        )

        fun removed(library: Library, progress: LibraryProgress) = CardQueue(
            library.questions.filter { progress.of(it.id).removed },
            library,
            progress,
        )
    }
}
