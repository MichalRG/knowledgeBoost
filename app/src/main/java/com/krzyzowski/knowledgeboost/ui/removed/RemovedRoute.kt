package com.krzyzowski.knowledgeboost.ui.removed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RemovedRoute(viewModel: RemovedViewModel = viewModel()) {
    val queue by viewModel.queue.collectAsStateWithLifecycle()
    RemovedScreen(queue = queue ?: return, onRestore = viewModel::restore)
}
