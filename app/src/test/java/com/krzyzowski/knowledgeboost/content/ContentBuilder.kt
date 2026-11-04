package com.krzyzowski.knowledgeboost.content

object ContentBuilder {
    fun question(id: String, topicId: String, text: String = "Question $id?"): Question = Question(
        id = id,
        topicId = topicId,
        text = text,
        answer = "Answer to $id",
        explanation = "Explanation of $id",
        code = emptyList(),
    )

    fun topic(id: String, questionIds: List<String>, category: String = "Databases", title: String = id): Topic =
        Topic(id, category = category, title = title, questions = questionIds.map { question(it, topicId = id) })

    fun library(vararg topics: Topic): Library = Library(topics.toList())
}
