package com.krzyzowski.knowledgeboost.ui.card

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.krzyzowski.knowledgeboost.content.Question

@Composable
fun QuestionCard(question: Question, revealed: Boolean) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                question.text,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.testTag("card_question"),
            )
            if (revealed) {
                Text(
                    question.answer,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("card_answer"),
                )
                Text(
                    question.explanation,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.testTag("card_explanation"),
                )
            }
            if (revealed && question.code.isNotEmpty()) {
                Text(
                    question.code.joinToString("\n"),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    softWrap = false,
                    modifier = Modifier
                        .testTag("card_code")
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.small)
                        .horizontalScroll(rememberScrollState())
                        .padding(12.dp),
                )
            }
        }
    }
}
