package com.krzyzowski.knowledgeboost.ui.topic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TopicRoute(
    topicId: String,
    viewModel: TopicViewModel = viewModel(key = "topic:$topicId") {
        TopicViewModel(checkNotNull(this[APPLICATION_KEY]), topicId)
    },
) {
    val queue by viewModel.queue.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val positions by viewModel.positions.collectAsStateWithLifecycle()
    val revealed by viewModel.revealed.collectAsStateWithLifecycle()
    val entered = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!entered.value) viewModel.hideAnswer()
        entered.value = true
    }

    val currentQueue = queue ?: return
    TopicScreen(
        title = viewModel.title,
        queue = currentQueue,
        position = currentQueue.positionOf((positions ?: return).of(topicId)),
        nextReturn = (summary ?: return).nextReturn,
        revealed = revealed,
        onReveal = viewModel::reveal,
        onMove = viewModel::move,
        onKnewIt = viewModel::knewIt,
        onMissed = viewModel::missed,
        onRemove = viewModel::remove,
    )
}
