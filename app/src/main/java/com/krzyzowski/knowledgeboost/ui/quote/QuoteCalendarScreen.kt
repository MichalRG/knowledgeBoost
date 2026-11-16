package com.krzyzowski.knowledgeboost.ui.quote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.krzyzowski.knowledgeboost.R
import com.krzyzowski.knowledgeboost.quote.QuoteHistoryState
import com.krzyzowski.knowledgeboost.ui.DateLabel
import java.time.LocalDate

@Composable
fun QuoteCalendarScreen(state: QuoteHistoryState, today: LocalDate) {
    var selectedDay by rememberSaveable { mutableStateOf<String?>(null) }
    var choosingDate by rememberSaveable { mutableStateOf(false) }
    val firstDay = state.history?.firstDay
    val dates = remember(firstDay, today) { QuoteCalendarDates(minOf(firstDay ?: today, today), today) }
    val selected = dates.selection(selectedDay?.let(LocalDate::parse) ?: today)
    val quote = state.history?.days?.get(selected)

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
                .verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.quote_calendar_title), style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.quote_calendar_intro))
            if (state.error != null) {
                Text(
                    stringResource(R.string.quote_history_error, state.error),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("quote_history_error"),
                )
            }
            if (firstDay == null) {
                if (state.error == null) Text(stringResource(R.string.quote_calendar_loading))
            } else {
                OutlinedButton(
                    onClick = { choosingDate = true },
                    modifier = Modifier.fillMaxWidth().testTag("quote_calendar_choose_date"),
                ) { Text(stringResource(R.string.quote_calendar_date, DateLabel.of(selected))) }
                OutlinedButton(
                    onClick = { selectedDay = null },
                    enabled = selected != today,
                    modifier = Modifier.fillMaxWidth().testTag("quote_calendar_today"),
                ) { Text(stringResource(R.string.quote_calendar_today)) }
                if (quote != null) {
                    DailyQuoteCard(quote, stringResource(R.string.quote_calendar_thought, DateLabel.of(selected)))
                } else {
                    Text(stringResource(R.string.quote_calendar_unavailable), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
    if (choosingDate && firstDay != null) {
        key(firstDay, today) {
            QuoteDatePicker(
                dates = dates,
                selected = selected,
                onSelect = { selectedDay = it.toString(); choosingDate = false },
                onDismiss = { choosingDate = false },
            )
        }
    }
}
