package com.krzyzowski.knowledgeboost.ui.missed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MissedCardRoute(startId: String, viewModel: MissedViewModel = viewModel()) {
    val queue by viewModel.queue.collectAsStateWithLifecycle()
    val today by viewModel.today.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()
    val revealed by viewModel.revealed.collectAsStateWithLifecycle()

    LaunchedEffect(startId) {
        if (viewModel.selected.value == null) viewModel.open(startId)
    }

    val currentQueue = queue ?: return
    MissedCardScreen(
        queue = currentQueue,
        position = currentQueue.positionOf(selected ?: startId),
        today = today,
        revealed = revealed,
        onReveal = viewModel::reveal,
        onMove = viewModel::move,
        onKnewIt = viewModel::knewIt,
        onMissed = viewModel::missed,
        onRemove = viewModel::remove,
        onClear = viewModel::clearCurrent,
    )
}
