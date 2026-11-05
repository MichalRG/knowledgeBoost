package com.krzyzowski.knowledgeboost.content

object TopicFileBuilder {
    fun file(name: String = "added", id: String = "$name-question", category: String = "Programming") = TopicFile(
        "$name.json",
        """{"category":"$category","title":"$name","questions":[
            {"id":"$id","question":"Question?","answer":"Answer","explanation":"Explanation","code":["first","","last"]}
        ]}""",
    )
}
