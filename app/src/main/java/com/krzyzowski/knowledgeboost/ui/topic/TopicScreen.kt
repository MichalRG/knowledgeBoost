package com.krzyzowski.knowledgeboost.ui.topic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.krzyzowski.knowledgeboost.R
import com.krzyzowski.knowledgeboost.session.CardQueue
import com.krzyzowski.knowledgeboost.ui.DateLabel
import com.krzyzowski.knowledgeboost.ui.card.CardBar
import com.krzyzowski.knowledgeboost.ui.card.CardNavigation
import com.krzyzowski.knowledgeboost.ui.card.GradeButtons
import com.krzyzowski.knowledgeboost.ui.card.QuestionCard
import java.time.LocalDate

@Composable
fun TopicScreen(
    title: String,
    queue: CardQueue,
    position: Int,
    nextReturn: LocalDate?,
    revealed: Boolean,
    onReveal: () -> Unit,
    onMove: (Int) -> Unit,
    onKnewIt: () -> Unit,
    onMissed: () -> Unit,
    onRemove: () -> Unit,
) {
    Scaffold(
        bottomBar = {
            if (queue.size > 0) {
                CardBar {
                    if (revealed) {
                        GradeButtons(
                            knewItDays = queue.progressAt(position).nextStep.days,
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
            Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.testTag("topic_title"))
            if (queue.size == 0) {
                Text(stringResource(R.string.topic_empty), modifier = Modifier.testTag("topic_empty"))
                if (nextReturn != null) {
                    Text(
                        stringResource(R.string.topic_next_return, DateLabel.of(nextReturn)),
                        modifier = Modifier.testTag("next_return"),
                    )
                }
            } else {
                QuestionCard(queue.at(position), revealed = revealed)
            }
        }
    }
}
