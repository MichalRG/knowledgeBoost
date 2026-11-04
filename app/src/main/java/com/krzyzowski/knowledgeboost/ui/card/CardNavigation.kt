package com.krzyzowski.knowledgeboost.ui.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.krzyzowski.knowledgeboost.R

@Composable
fun CardNavigation(position: Int, size: Int, onMove: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(
            onClick = { onMove(-1) },
            enabled = position > 0,
            modifier = Modifier.weight(1f).testTag("previous"),
        ) {
            Text(stringResource(R.string.card_previous))
        }
        Text(stringResource(R.string.card_position, position + 1, size), modifier = Modifier.testTag("card_position"))
        OutlinedButton(
            onClick = { onMove(1) },
            enabled = position < size - 1,
            modifier = Modifier.weight(1f).testTag("next"),
        ) {
            Text(stringResource(R.string.card_next))
        }
    }
}
