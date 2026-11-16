package com.krzyzowski.knowledgeboost.quote

import com.krzyzowski.knowledgeboost.content.TopicLibraryStore
import com.krzyzowski.knowledgeboost.data.DailyQuoteHistoryStore
import com.krzyzowski.knowledgeboost.data.DailyQuoteStore
import com.krzyzowski.knowledgeboost.time.Today
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.io.IOException

class DailyQuoteRecorder(
    private val store: DailyQuoteHistoryStore,
    private val anchor: DailyQuoteStore,
    private val library: TopicLibraryStore,
    private val today: Today,
) {
    private val currentState = MutableStateFlow(QuoteHistoryState())
    val state = currentState.asStateFlow()

    suspend fun run() {
        try {
            anchor.start(today.date.value)
            val start = requireNotNull(anchor.startedOn.first())
            currentState.value = QuoteHistoryState(store.history.first())
            combine(today.date, library.library, library.state) { day, _, _ -> day }.collect { day ->
                val folder = library.state.value
                if (!folder.initialized || folder.loading) return@collect
                val knownQuotes = currentState.value.history?.quotes.orEmpty()
                if (folder.error != null && knownQuotes.isEmpty()) {
                    currentState.value = currentState.value.copy(error = folder.error)
                    return@collect
                }
                val quotes = if (folder.error != null) knownQuotes else library.library.value.quotes
                currentState.value = QuoteHistoryState(store.record(start, day, quotes))
            }
        } catch (error: IOException) {
            currentState.value = currentState.value.copy(error = error.message ?: error.javaClass.simpleName)
        } catch (error: IllegalArgumentException) {
            currentState.value = currentState.value.copy(error = error.message ?: error.javaClass.simpleName)
        }
    }
}
