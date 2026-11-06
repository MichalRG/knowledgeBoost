package com.krzyzowski.knowledgeboost.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class DailyQuoteStore(private val dataStore: DataStore<Preferences>) {
    val startedOn: Flow<LocalDate?> = dataStore.data.map { preferences ->
        preferences[START_DAY]?.let(LocalDate::parse)
    }

    suspend fun start(day: LocalDate) {
        dataStore.edit { preferences ->
            if (preferences[START_DAY] == null) preferences[START_DAY] = day.toString()
        }
    }

    private companion object {
        val START_DAY = stringPreferencesKey("daily_quote_start_day_v1")
    }
}
