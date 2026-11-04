package com.krzyzowski.knowledgeboost.ui.topic

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.krzyzowski.knowledgeboost.KnowledgeBoostApp
import com.krzyzowski.knowledgeboost.content.Topic
import com.krzyzowski.knowledgeboost.session.CardQueue
import com.krzyzowski.knowledgeboost.session.ResumePositions
import com.krzyzowski.knowledgeboost.session.TopicSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TopicViewModel(application: Application, private val topicId: String) : AndroidViewModel(application) {
    private val container = (application as KnowledgeBoostApp).container
    private val topic = container.library.topic(topicId) ?: Topic(topicId, category = "", title = "", questions = emptyList())
    private val _revealed = MutableStateFlow(false)

    val title: String = topic.title

    val revealed: StateFlow<Boolean> = _revealed.asStateFlow()

    val queue: StateFlow<CardQueue?> =
        combine(container.progressStore.progress, container.today.date) { progress, day ->
            CardQueue.available(topic, container.library, progress, day)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val summary: StateFlow<TopicSummary?> =
        combine(container.progressStore.progress, container.today.date) { progress, day ->
            TopicSummary.of(topic, progress, day)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val positions: StateFlow<ResumePositions?> = container.resumeStore.positions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun reveal() {
        _revealed.value = true
    }

    fun hideAnswer() {
        _revealed.value = false
    }

    fun move(step: Int) {
        val current = queue.value ?: return
        val target = current.positionOf(positions.value?.of(topicId)) + step
        if (target !in 0 until current.size) return
        viewModelScope.launch { show(current.at(target).id) }
    }

    fun knewIt() = grade(container.progressStore::knewIt)

    fun missed() = grade(container.progressStore::missed)

    fun remove() = grade(container.progressStore::remove)

    private fun grade(action: suspend (String) -> Unit) {
        val current = queue.value?.takeIf { it.size > 0 } ?: return
        val position = current.positionOf(positions.value?.of(topicId))
        val graded = current.at(position).id
        viewModelScope.launch {
            show(current.after(position))
            action(graded)
        }
    }

    private suspend fun show(questionId: String) {
        container.resumeStore.set(topicId, questionId)
        hideAnswer()
    }
}
