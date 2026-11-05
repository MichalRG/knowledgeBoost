package com.krzyzowski.knowledgeboost.ui.folder

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.krzyzowski.knowledgeboost.KnowledgeBoostApp
import kotlinx.coroutines.launch

class TopicFolderViewModel(application: Application) : AndroidViewModel(application) {
    private val store = (application as KnowledgeBoostApp).container.libraryStore

    val state = store.state
    val bundled = store.bundled

    fun select(uri: String) {
        viewModelScope.launch { store.select(uri) }
    }

    fun reload() {
        viewModelScope.launch { store.reload() }
    }

    fun disconnect() {
        viewModelScope.launch { store.disconnect() }
    }
}
