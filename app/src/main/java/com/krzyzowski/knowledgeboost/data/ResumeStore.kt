package com.krzyzowski.knowledgeboost.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.krzyzowski.knowledgeboost.session.ResumePositions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ResumeStore(private val dataStore: DataStore<Preferences>) {
    val positions: Flow<ResumePositions> = dataStore.data.map { it.positions() }

    suspend fun set(topicId: String, questionId: String) {
        dataStore.edit { prefs -> prefs[TOPIC_RESUME] = prefs.positions().with(topicId, questionId).encode() }
    }

    private fun Preferences.positions(): ResumePositions =
        this[TOPIC_RESUME]?.let(ResumePositions::decode) ?: ResumePositions.NONE

    private companion object {
        val TOPIC_RESUME = stringPreferencesKey("topic_resume_v1")
    }
}
