package com.krzyzowski.knowledgeboost.content

class Library(topics: List<Topic>, val quotes: List<DailyQuote> = emptyList()) {
    val topics: List<Topic> = topics.sortedWith(
        compareBy(String.CASE_INSENSITIVE_ORDER, Topic::category)
            .thenBy(String.CASE_INSENSITIVE_ORDER, Topic::title)
            .thenBy(Topic::id),
    )

    val categories: List<String> = this.topics.map { it.category }.distinct()

    val questions: List<Question> = this.topics.flatMap { it.questions }

    private val topicsById = this.topics.associateBy { it.id }

    private val order = questions.withIndex().associate { (index, question) -> question.id to index }

    fun topicsIn(category: String): List<Topic> = topics.filter { it.category == category }

    fun topic(id: String): Topic? = topicsById[id]

    fun topicOf(question: Question): Topic = topicsById.getValue(question.topicId)

    fun orderOf(questionId: String): Int? = order[questionId]
}
