package com.krzyzowski.knowledgeboost.time

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

class Today(private val clock: AppClock) {
    private val _date = MutableStateFlow(clock.today())

    val date: StateFlow<LocalDate> = _date.asStateFlow()

    fun refresh() {
        _date.value = clock.today()
    }
}
