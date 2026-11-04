package com.krzyzowski.knowledgeboost.session

import com.krzyzowski.knowledgeboost.content.Topic
import com.krzyzowski.knowledgeboost.schedule.LibraryProgress
import java.time.LocalDate

data class TopicSummary(val available: Int, val hidden: Int, val missed: Int, val nextReturn: LocalDate?) {
    companion object {
        fun of(topic: Topic, progress: LibraryProgress, today: LocalDate): TopicSummary {
            val kept = topic.questions.map { progress.of(it.id) }.filterNot { it.removed }
            val hidden = kept.filter { it.isHiddenOn(today) }
            return TopicSummary(
                available = kept.size - hidden.size,
                hidden = hidden.size,
                missed = kept.count { it.missed },
                nextReturn = hidden.mapNotNull { it.hiddenUntil }.minOrNull(),
            )
        }
    }
}
