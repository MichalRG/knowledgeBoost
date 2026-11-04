package com.krzyzowski.knowledgeboost.ui.category

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.krzyzowski.knowledgeboost.KnowledgeBoostApp
import com.krzyzowski.knowledgeboost.content.Topic
import com.krzyzowski.knowledgeboost.session.TopicSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class CategoryViewModel(application: Application, name: String) : AndroidViewModel(application) {
    private val container = (application as KnowledgeBoostApp).container
    private val topics = container.library.topicsIn(name)

    val summaries: StateFlow<List<Pair<Topic, TopicSummary>>?> =
        combine(container.progressStore.progress, container.today.date) { progress, day ->
            topics.map { it to TopicSummary.of(it, progress, day) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
