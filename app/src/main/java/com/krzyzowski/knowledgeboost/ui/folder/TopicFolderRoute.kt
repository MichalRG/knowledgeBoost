package com.krzyzowski.knowledgeboost.ui.folder

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TopicFolderRoute(viewModel: TopicFolderViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) viewModel.select(uri.toString())
    }
    TopicFolderScreen(
        state = state,
        folderName = state.uri?.let { Uri.decode(it.substringAfterLast('/')) },
        bundledTopics = viewModel.bundled.topics.size,
        bundledQuestions = viewModel.bundled.questions.size,
        onChoose = { picker.launch(state.uri?.toUri()) },
        onReload = viewModel::reload,
        onDisconnect = viewModel::disconnect,
    )
}
