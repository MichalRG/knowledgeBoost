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
    val summaries: StateFlow<List<Pair<Topic, TopicSummary>>?> =
        combine(container.library, container.progressStore.progress, container.today.date) { library, progress, day ->
            library.topicsIn(name).map { it to TopicSummary.of(it, progress, day) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
