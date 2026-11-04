package com.krzyzowski.knowledgeboost.ui.missed

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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.krzyzowski.knowledgeboost.R
import com.krzyzowski.knowledgeboost.session.CardQueue
import java.time.LocalDate

@Composable
fun MissedScreen(queue: CardQueue, today: LocalDate, onOpen: (String) -> Unit, onClear: (String) -> Unit) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.missed_title), style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.missed_intro))
            if (queue.size == 0) {
                Text(stringResource(R.string.missed_empty), modifier = Modifier.testTag("missed_empty"))
            }
            queue.cards.forEachIndexed { position, question ->
                val topic = queue.topicAt(position)
                Card(
                    onClick = { onOpen(question.id) },
                    modifier = Modifier.fillMaxWidth().testTag("missed_row_${question.id}"),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(question.text, style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(R.string.question_where, topic.category, topic.title),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.testTag("missed_where_${question.id}"),
                        )
                        MissedStatus(
                            queue.progressAt(position),
                            today,
                            Modifier.testTag("missed_status_${question.id}"),
                        )
                        TextButton(
                            onClick = { onClear(question.id) },
                            modifier = Modifier.testTag("clear_missed_${question.id}"),
                        ) {
                            Text(stringResource(R.string.missed_clear))
                        }
                    }
                }
            }
        }
    }
}
