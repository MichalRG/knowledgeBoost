package com.krzyzowski.knowledgeboost.ui.topic

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.krzyzowski.knowledgeboost.R

@Composable
fun MissingTopicScreen() {
    Scaffold { padding ->
        Text(stringResource(R.string.topic_missing), modifier = Modifier.padding(padding).padding(24.dp))
    }
}
