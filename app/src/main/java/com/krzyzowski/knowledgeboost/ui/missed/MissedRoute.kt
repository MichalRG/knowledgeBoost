package com.krzyzowski.knowledgeboost.ui.missed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MissedRoute(onOpenCard: (String) -> Unit, viewModel: MissedViewModel = viewModel()) {
    val queue by viewModel.queue.collectAsStateWithLifecycle()
    val today by viewModel.today.collectAsStateWithLifecycle()
    MissedScreen(
        queue = queue ?: return,
        today = today,
        onOpen = { questionId ->
            viewModel.open(questionId)
            onOpenCard(questionId)
        },
        onClear = viewModel::clearMissed,
    )
}
