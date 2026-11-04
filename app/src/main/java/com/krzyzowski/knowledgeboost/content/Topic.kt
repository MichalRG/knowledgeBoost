package com.krzyzowski.knowledgeboost.content

data class Topic(val id: String, val category: String, val title: String, val questions: List<Question>)
