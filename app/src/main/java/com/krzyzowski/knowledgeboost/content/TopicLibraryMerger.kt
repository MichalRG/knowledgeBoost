package com.krzyzowski.knowledgeboost.content

class TopicLibraryMerger(private val parser: TopicParser = TopicParser()) {
    fun merge(bundled: Library, files: List<TopicFile>): Library {
        val topics = bundled.topics + files.sortedBy { it.name }.map { parser.parse(it.name, it.text) }
        val topicIds = mutableSetOf<String>()
        val questionOwners = mutableMapOf<String, String>()
        topics.forEach { topic ->
            require(topicIds.add(topic.id)) {
                "${topic.id}.json: the topic id already exists. Use a unique file name."
            }
            topic.questions.forEach { question ->
                val owner = questionOwners.putIfAbsent(question.id, topic.id)
                require(owner == null) {
                    "${topic.id}.json: question id ${question.id} is already used in $owner.json."
                }
            }
        }
        return Library(topics)
    }
}
