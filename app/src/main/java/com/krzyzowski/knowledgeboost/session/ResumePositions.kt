package com.krzyzowski.knowledgeboost.session

class ResumePositions private constructor(private val positions: Map<String, String>) {
    fun of(topicId: String): String? = positions[topicId]

    fun with(topicId: String, questionId: String): ResumePositions =
        ResumePositions(positions + (topicId to questionId))

    fun encode(): String = positions.entries
        .sortedBy { it.key }
        .joinToString(",") { (topicId, questionId) -> "$topicId=$questionId" }

    companion object {
        val NONE = ResumePositions(emptyMap())

        fun decode(encoded: String): ResumePositions = ResumePositions(
            encoded.split(',')
                .map { it.split('=') }
                .filter { it.size == 2 && it.none(String::isBlank) }
                .associate { (topicId, questionId) -> topicId to questionId },
        )
    }
}
