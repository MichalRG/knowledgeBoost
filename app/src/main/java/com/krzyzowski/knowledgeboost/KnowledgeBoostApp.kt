package com.krzyzowski.knowledgeboost

import android.app.Application

class KnowledgeBoostApp : Application() {
    val container by lazy { AppContainer(this) }
}
