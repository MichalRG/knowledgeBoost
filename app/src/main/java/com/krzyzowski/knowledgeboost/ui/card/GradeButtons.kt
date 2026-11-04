package com.krzyzowski.knowledgeboost.ui.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.krzyzowski.knowledgeboost.R

@Composable
fun GradeButtons(knewItDays: Int?, onKnewIt: () -> Unit, onMissed: () -> Unit, onRemove: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onMissed, modifier = Modifier.weight(1f).testTag("mark_missed")) {
                Text(stringResource(R.string.card_missed))
            }
            Button(onClick = onKnewIt, modifier = Modifier.weight(1f).testTag("knew_it")) {
                Text(stringResource(R.string.card_knew_it))
            }
        }
        if (knewItDays != null) {
            Text(
                pluralStringResource(R.plurals.knew_it_hides_days, knewItDays, knewItDays),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("knew_it_days"),
            )
        }
        TextButton(onClick = onRemove, modifier = Modifier.testTag("remove")) {
            Text(stringResource(R.string.card_remove))
        }
    }
}
