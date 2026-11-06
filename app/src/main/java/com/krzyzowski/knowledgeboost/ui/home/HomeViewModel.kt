package com.krzyzowski.knowledgeboost.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.krzyzowski.knowledgeboost.KnowledgeBoostApp
import com.krzyzowski.knowledgeboost.content.DailyQuote
import com.krzyzowski.knowledgeboost.session.CardQueue
import com.krzyzowski.knowledgeboost.time.DailyQuoteCycle
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as KnowledgeBoostApp).container

    val categories: StateFlow<List<String>> = container.library
        .map { it.categories }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), container.library.value.categories)

    val folder = container.libraryStore.state
    private val cycle = DailyQuoteCycle()

    val quote: StateFlow<DailyQuote?> =
        combine(container.library, container.today.date, container.dailyQuoteStore.startedOn) { library, day, start ->
            start?.let { cycle.onDay(library.quotes, it, day) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch { container.dailyQuoteStore.start(container.clock.today()) }
    }

    val missed: StateFlow<Int?> =
        combine(container.library, container.progressStore.progress) { library, progress ->
            CardQueue.missed(library, progress).size
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val removed: StateFlow<Int?> =
        combine(container.library, container.progressStore.progress) { library, progress ->
            CardQueue.removed(library, progress).size
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
