package com.krzyzowski.knowledgeboost.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.krzyzowski.knowledgeboost.ui.Destination

@Composable
fun HomeRoute(onOpen: (Destination) -> Unit, viewModel: HomeViewModel = viewModel()) {
    val missed by viewModel.missed.collectAsStateWithLifecycle()
    val removed by viewModel.removed.collectAsStateWithLifecycle()
    HomeScreen(
        categories = viewModel.categories,
        missed = missed ?: return,
        removed = removed ?: return,
        onOpen = onOpen,
    )
}
