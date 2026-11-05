package com.krzyzowski.knowledgeboost.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TopicFolderStore(private val dataStore: DataStore<Preferences>) {
    val uri: Flow<String?> = dataStore.data.map { it[FOLDER_URI] }

    suspend fun set(uri: String?) {
        dataStore.edit { preferences ->
            if (uri == null) preferences.remove(FOLDER_URI) else preferences[FOLDER_URI] = uri
        }
    }

    private companion object {
        val FOLDER_URI = stringPreferencesKey("topic_folder_uri_v1")
    }
}
