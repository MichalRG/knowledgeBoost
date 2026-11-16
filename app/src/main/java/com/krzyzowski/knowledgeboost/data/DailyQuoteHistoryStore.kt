package com.krzyzowski.knowledgeboost.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.krzyzowski.knowledgeboost.content.DailyQuote
import com.krzyzowski.knowledgeboost.quote.DailyQuoteHistory
import com.krzyzowski.knowledgeboost.quote.DailyQuoteHistoryJson
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class DailyQuoteHistoryStore(private val dataStore: DataStore<Preferences>) {
    private val json = DailyQuoteHistoryJson()
    val history = dataStore.data.map(::read)

    suspend fun record(startedOn: LocalDate, today: LocalDate, quotes: List<DailyQuote>): DailyQuoteHistory =
        read(dataStore.edit { preferences ->
            preferences[HISTORY] = json.encode(read(preferences).record(startedOn, today, quotes))
        })

    private fun read(preferences: Preferences): DailyQuoteHistory =
        preferences[HISTORY]?.let(json::decode) ?: DailyQuoteHistory()

    private companion object {
        val HISTORY = stringPreferencesKey("daily_quote_history_v1")
    }
}
