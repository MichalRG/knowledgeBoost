package com.krzyzowski.knowledgeboost.ui.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.krzyzowski.knowledgeboost.R
import com.krzyzowski.knowledgeboost.content.Topic
import com.krzyzowski.knowledgeboost.session.TopicSummary

@Composable
fun CategoryScreen(name: String, summaries: List<Pair<Topic, TopicSummary>>, onOpenTopic: (String) -> Unit) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(name, style = MaterialTheme.typography.headlineMedium)
            summaries.forEach { (topic, summary) ->
                Card(
                    onClick = { onOpenTopic(topic.id) },
                    modifier = Modifier.fillMaxWidth().testTag("topic_${topic.id}"),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(topic.title, style = MaterialTheme.typography.titleLarge)
                        Text(
                            stringResource(R.string.category_available, summary.available),
                            modifier = Modifier.testTag("available_${topic.id}"),
                        )
                        Text(
                            stringResource(R.string.category_hidden, summary.hidden),
                            modifier = Modifier.testTag("hidden_${topic.id}"),
                        )
                        Text(
                            stringResource(R.string.category_missed, summary.missed),
                            modifier = Modifier.testTag("missed_${topic.id}"),
                        )
                    }
                }
            }
        }
    }
}
