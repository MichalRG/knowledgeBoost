package com.krzyzowski.knowledgeboost.ui.missed

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.krzyzowski.knowledgeboost.KnowledgeBoostApp
import com.krzyzowski.knowledgeboost.session.CardQueue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class MissedViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as KnowledgeBoostApp).container
    private val _selected = MutableStateFlow<String?>(null)
    private val _revealed = MutableStateFlow(false)

    val queue: StateFlow<CardQueue?> = container.progressStore.progress
        .map { CardQueue.missed(container.library, it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val today: StateFlow<LocalDate> = container.today.date

    val selected: StateFlow<String?> = _selected.asStateFlow()

    val revealed: StateFlow<Boolean> = _revealed.asStateFlow()

    fun open(questionId: String) {
        _selected.value = questionId
        _revealed.value = false
    }

    fun reveal() {
        _revealed.value = true
    }

    fun clearMissed(questionId: String) {
        viewModelScope.launch { container.progressStore.clearMissed(questionId) }
    }

    fun move(step: Int) {
        val current = queue.value ?: return
        val target = current.positionOf(_selected.value) + step
        if (target in 0 until current.size) open(current.at(target).id)
    }

    fun knewIt() = grade(container.progressStore::knewIt)

    fun missed() = grade(container.progressStore::missed)

    fun remove() = grade(container.progressStore::remove)

    fun clearCurrent() = grade(container.progressStore::clearMissed)

    private fun grade(action: suspend (String) -> Unit) {
        val current = queue.value?.takeIf { it.size > 0 } ?: return
        val position = current.positionOf(_selected.value)
        val graded = current.at(position).id
        open(current.after(position))
        viewModelScope.launch { action(graded) }
    }
}
