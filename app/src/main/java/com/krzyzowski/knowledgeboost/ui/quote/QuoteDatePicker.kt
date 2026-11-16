package com.krzyzowski.knowledgeboost.ui.quote

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.krzyzowski.knowledgeboost.R
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteDatePicker(
    dates: QuoteCalendarDates,
    selected: LocalDate,
    onSelect: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val picker = rememberDatePickerState(
        initialSelectedDateMillis = QuoteCalendarDates.millis(selected),
        initialDisplayedMonthMillis = QuoteCalendarDates.millis(selected),
        yearRange = dates.firstDay.year..dates.today.year,
        selectableDates = dates,
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = picker.selectedDateMillis?.let(dates::isSelectableDate) == true,
                onClick = {
                    picker.selectedDateMillis?.let { onSelect(QuoteCalendarDates.date(it)) }
                },
                modifier = Modifier.testTag("quote_calendar_confirm"),
            ) { Text(stringResource(R.string.quote_calendar_select)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.quote_calendar_cancel)) }
        },
    ) {
        DatePicker(state = picker)
    }
}
