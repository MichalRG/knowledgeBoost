package com.krzyzowski.knowledgeboost.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.krzyzowski.knowledgeboost.R
import com.krzyzowski.knowledgeboost.ui.Destination

@Composable
fun HomeScreen(categories: List<String>, missed: Int, removed: Int, folderError: String?, onOpen: (Destination) -> Unit) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
            categories.forEach { category ->
                FilledTonalButton(
                    onClick = { onOpen(Destination.Category(category)) },
                    modifier = Modifier.fillMaxWidth().testTag("category_$category"),
                ) {
                    Text(category, style = MaterialTheme.typography.titleMedium)
                }
            }
            OutlinedButton(
                onClick = { onOpen(Destination.Missed) },
                modifier = Modifier.fillMaxWidth().testTag("open_missed"),
            ) {
                Text(stringResource(R.string.home_missed, missed))
            }
            OutlinedButton(
                onClick = { onOpen(Destination.Removed) },
                modifier = Modifier.fillMaxWidth().testTag("open_removed"),
            ) {
                Text(stringResource(R.string.home_removed, removed))
            }
            OutlinedButton(
                onClick = { onOpen(Destination.TopicFolder) },
                modifier = Modifier.fillMaxWidth().testTag("open_topic_folder"),
            ) {
                Text(stringResource(R.string.folder_title))
            }
            if (folderError != null) {
                Text(
                    stringResource(R.string.folder_error, folderError),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("home_folder_error"),
                )
            }
        }
    }
}
