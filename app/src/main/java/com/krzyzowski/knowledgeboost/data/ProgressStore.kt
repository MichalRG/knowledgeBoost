package com.krzyzowski.knowledgeboost.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.krzyzowski.knowledgeboost.schedule.LibraryProgress
import com.krzyzowski.knowledgeboost.time.AppClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProgressStore(private val dataStore: DataStore<Preferences>, private val clock: AppClock) {
    val progress: Flow<LibraryProgress> = dataStore.data.map { it.progress() }

    suspend fun knewIt(id: String) = update { it.knewIt(id, clock.today()) }

    suspend fun missed(id: String) = update { it.missed(id) }

    suspend fun remove(id: String) = update { it.remove(id) }

    suspend fun restore(id: String) = update { it.restore(id) }

    suspend fun clearMissed(id: String) = update { it.clearMissed(id) }

    private suspend fun update(change: (LibraryProgress) -> LibraryProgress) {
        dataStore.edit { prefs -> prefs[QUESTION_PROGRESS] = change(prefs.progress()).encode() }
    }

    private fun Preferences.progress(): LibraryProgress =
        this[QUESTION_PROGRESS]?.let(LibraryProgress::decode) ?: LibraryProgress.NONE

    private companion object {
        val QUESTION_PROGRESS = stringPreferencesKey("question_progress_v1")
    }
}
