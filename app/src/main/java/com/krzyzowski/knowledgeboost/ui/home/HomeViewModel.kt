package com.krzyzowski.knowledgeboost.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.krzyzowski.knowledgeboost.KnowledgeBoostApp
import com.krzyzowski.knowledgeboost.session.CardQueue
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as KnowledgeBoostApp).container

    val categories: List<String> = container.library.categories

    val missed: StateFlow<Int?> = container.progressStore.progress
        .map { CardQueue.missed(container.library, it).size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val removed: StateFlow<Int?> = container.progressStore.progress
        .map { CardQueue.removed(container.library, it).size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
