package com.krzyzowski.knowledgeboost.content

import android.content.res.AssetManager

class AssetTopicSource(private val assets: AssetManager, private val parser: TopicParser) {
    fun load(): Library = Library(
        assets.list(DIRECTORY).orEmpty().sorted().mapNotNull { fileName ->
            runCatching {
                parser.parse(fileName, assets.open("$DIRECTORY/$fileName").bufferedReader().use { it.readText() })
            }.getOrNull()
        },
    )

    private companion object {
        const val DIRECTORY = "topics"
    }
}
