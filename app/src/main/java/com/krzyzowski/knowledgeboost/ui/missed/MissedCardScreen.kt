package com.krzyzowski.knowledgeboost.ui.missed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import com.krzyzowski.knowledgeboost.session.CardQueue
import com.krzyzowski.knowledgeboost.ui.card.CardBar
import com.krzyzowski.knowledgeboost.ui.card.CardNavigation
import com.krzyzowski.knowledgeboost.ui.card.GradeButtons
import com.krzyzowski.knowledgeboost.ui.card.QuestionCard
import java.time.LocalDate

@Composable
fun MissedCardScreen(
    queue: CardQueue,
    position: Int,
    today: LocalDate,
    revealed: Boolean,
    onReveal: () -> Unit,
    onMove: (Int) -> Unit,
    onKnewIt: () -> Unit,
    onMissed: () -> Unit,
    onRemove: () -> Unit,
    onClear: () -> Unit,
) {
    Scaffold(
        bottomBar = {
            if (queue.size > 0) {
                val progress = queue.progressAt(position)
                CardBar {
                    if (revealed) {
                        GradeButtons(
                            knewItDays = progress.nextStep.days.takeUnless { progress.isHiddenOn(today) },
                            onKnewIt = onKnewIt,
                            onMissed = onMissed,
                            onRemove = onRemove,
                        )
                    } else {
                        Button(onClick = onReveal, modifier = Modifier.fillMaxWidth().testTag("show_answer")) {
                            Text(stringResource(R.string.card_show_answer))
                        }
                    }
                    CardNavigation(position = position, size = queue.size, onMove = onMove)
                    OutlinedButton(onClick = onClear, modifier = Modifier.fillMaxWidth().testTag("clear_missed")) {
                        Text(stringResource(R.string.missed_clear_from_list))
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.missed_title), style = MaterialTheme.typography.headlineMedium)
            if (queue.size == 0) {
                Text(stringResource(R.string.missed_empty), modifier = Modifier.testTag("missed_empty"))
            } else {
                val topic = queue.topicAt(position)
                Text(
                    stringResource(R.string.question_where, topic.category, topic.title),
                    modifier = Modifier.testTag("card_where"),
                )
                MissedStatus(queue.progressAt(position), today, Modifier.testTag("card_status"))
                QuestionCard(queue.at(position), revealed = revealed)
            }
        }
    }
}
