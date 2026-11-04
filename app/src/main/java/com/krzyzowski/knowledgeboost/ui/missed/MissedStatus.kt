package com.krzyzowski.knowledgeboost.ui.missed

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.krzyzowski.knowledgeboost.R
import com.krzyzowski.knowledgeboost.schedule.QuestionProgress
import com.krzyzowski.knowledgeboost.ui.DateLabel
import java.time.LocalDate

@Composable
fun MissedStatus(progress: QuestionProgress, today: LocalDate, modifier: Modifier) {
    val hiddenUntil = progress.hiddenUntil
    Text(
        if (hiddenUntil != null && progress.isHiddenOn(today)) {
            stringResource(R.string.missed_hidden_until, DateLabel.of(hiddenUntil))
        } else {
            stringResource(R.string.missed_available)
        },
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier,
    )
}
