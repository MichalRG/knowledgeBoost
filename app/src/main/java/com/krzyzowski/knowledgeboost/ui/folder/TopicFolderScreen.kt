package com.krzyzowski.knowledgeboost.ui.folder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.krzyzowski.knowledgeboost.R
import com.krzyzowski.knowledgeboost.content.TopicFolderState

@Composable
fun TopicFolderScreen(
    state: TopicFolderState,
    folderName: String?,
    bundledTopics: Int,
    bundledQuestions: Int,
    onChoose: () -> Unit,
    onReload: () -> Unit,
    onDisconnect: () -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.folder_title), style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.folder_intro))
            Text(stringResource(R.string.folder_selected, folderName ?: stringResource(R.string.folder_none)))
            Text(
                stringResource(
                    R.string.folder_bundled_count,
                    pluralStringResource(R.plurals.folder_topic_count, bundledTopics, bundledTopics),
                    pluralStringResource(R.plurals.folder_question_count, bundledQuestions, bundledQuestions),
                ),
            )
            Text(
                stringResource(
                    R.string.folder_loaded_count,
                    pluralStringResource(R.plurals.folder_topic_count, state.topics, state.topics),
                    pluralStringResource(R.plurals.folder_question_count, state.questions, state.questions),
                ),
                modifier = Modifier.testTag("folder_loaded_count"),
            )
            if (state.loading) CircularProgressIndicator(modifier = Modifier.testTag("folder_loading"))
            if (state.error != null) {
                Text(
                    stringResource(R.string.folder_error, state.error),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("folder_error"),
                )
            }
            Button(
                onClick = onChoose,
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth().testTag("choose_topic_folder"),
            ) {
                Text(stringResource(R.string.folder_choose))
            }
            OutlinedButton(
                onClick = onReload,
                enabled = !state.loading && state.uri != null,
                modifier = Modifier.fillMaxWidth().testTag("reload_topic_folder"),
            ) {
                Text(stringResource(R.string.folder_reload))
            }
            OutlinedButton(
                onClick = onDisconnect,
                enabled = !state.loading && state.uri != null,
                modifier = Modifier.fillMaxWidth().testTag("disconnect_topic_folder"),
            ) {
                Text(stringResource(R.string.folder_disconnect))
            }
            Text(stringResource(R.string.folder_rules))
        }
    }
}
