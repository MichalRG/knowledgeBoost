package com.krzyzowski.knowledgeboost.content

data class Question(
    val id: String,
    val topicId: String,
    val text: String,
    val answer: String,
    val explanation: String,
    val code: List<String>,
)
