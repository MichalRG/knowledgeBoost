package com.krzyzowski.knowledgeboost.content

interface TopicFolderSource {
    fun read(uri: String): List<TopicFile>

    fun retain(uri: String)

    fun release(uri: String)
}
