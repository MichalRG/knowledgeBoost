package com.krzyzowski.knowledgeboost.ui.category

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CategoryRoute(
    name: String,
    onOpenTopic: (String) -> Unit,
    viewModel: CategoryViewModel = viewModel(key = "category:$name") {
        CategoryViewModel(checkNotNull(this[APPLICATION_KEY]), name)
    },
) {
    val summaries by viewModel.summaries.collectAsStateWithLifecycle()
    CategoryScreen(name = name, summaries = summaries ?: return, onOpenTopic = onOpenTopic)
}
