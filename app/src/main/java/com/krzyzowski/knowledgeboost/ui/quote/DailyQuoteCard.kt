package com.krzyzowski.knowledgeboost.ui.quote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.krzyzowski.knowledgeboost.R
import com.krzyzowski.knowledgeboost.content.DailyQuote

@Composable
fun DailyQuoteCard(quote: DailyQuote) {
    Card(modifier = Modifier.fillMaxWidth().testTag("daily_quote")) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                stringResource(R.string.quote_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(quote.text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.testTag("daily_quote_text"))
            if (quote.polish != null) {
                Text(stringResource(R.string.quote_polish, quote.polish), style = MaterialTheme.typography.bodyMedium)
            }
            Text(quote.author, style = MaterialTheme.typography.labelLarge, modifier = Modifier.testTag("daily_quote_author"))
            Text(
                stringResource(R.string.quote_source, quote.source),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("daily_quote_source"),
            )
        }
    }
}
