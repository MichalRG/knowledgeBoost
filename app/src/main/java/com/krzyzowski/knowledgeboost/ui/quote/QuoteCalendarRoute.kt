package com.krzyzowski.knowledgeboost.ui.quote

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.krzyzowski.knowledgeboost.quote.QuoteHistoryState
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

@Composable
fun QuoteCalendarRoute(history: StateFlow<QuoteHistoryState>, today: StateFlow<LocalDate>) {
    val state by history.collectAsStateWithLifecycle()
    val day by today.collectAsStateWithLifecycle()
    QuoteCalendarScreen(state, day)
}
