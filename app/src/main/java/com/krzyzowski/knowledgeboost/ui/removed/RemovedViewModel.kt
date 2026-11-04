package com.krzyzowski.knowledgeboost.ui.removed

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.krzyzowski.knowledgeboost.KnowledgeBoostApp
import com.krzyzowski.knowledgeboost.session.CardQueue
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RemovedViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as KnowledgeBoostApp).container

    val queue: StateFlow<CardQueue?> = container.progressStore.progress
        .map { CardQueue.removed(container.library, it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun restore(questionId: String) {
        viewModelScope.launch { container.progressStore.restore(questionId) }
    }
}
