package com.krzyzowski.knowledgeboost.content

data class TopicFolderState(
    val uri: String? = null,
    val topics: Int = 0,
    val questions: Int = 0,
    val loading: Boolean = false,
    val error: String? = null,
)
