package com.krzyzowski.knowledgeboost.content

import java.io.IOException

class FakeTopicFolder : TopicFolderSource {
    val folders = mutableMapOf("folder" to listOf(TopicFileBuilder.file()))
    val retained = mutableListOf<String>()
    val released = mutableListOf<String>()
    var readError: Exception? = null
    var retainError: SecurityException? = null

    override fun read(uri: String): List<TopicFile> {
        readError?.let { throw it }
        return folders[uri] ?: throw IOException("Folder not found: $uri")
    }

    override fun retain(uri: String) {
        retainError?.let { throw it }
        retained += uri
    }

    override fun release(uri: String) {
        released += uri
    }
}
