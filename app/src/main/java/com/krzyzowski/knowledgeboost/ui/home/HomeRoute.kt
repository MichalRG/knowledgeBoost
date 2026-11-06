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
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val folder by viewModel.folder.collectAsStateWithLifecycle()
    val quote by viewModel.quote.collectAsStateWithLifecycle()
    HomeScreen(
        categories = categories,
        missed = missed ?: return,
        removed = removed ?: return,
        folderError = folder.error,
        quote = quote,
        onOpen = onOpen,
    )
}
