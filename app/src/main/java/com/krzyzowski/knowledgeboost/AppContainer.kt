package com.krzyzowski.knowledgeboost

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import com.krzyzowski.knowledgeboost.content.AssetTopicSource
import com.krzyzowski.knowledgeboost.content.TopicParser
import com.krzyzowski.knowledgeboost.data.ProgressStore
import com.krzyzowski.knowledgeboost.data.ResumeStore
import com.krzyzowski.knowledgeboost.time.Today
import com.krzyzowski.knowledgeboost.time.VariantClock

class AppContainer(context: Context) {
    private val dataStore = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("knowledgeboost") },
    )

    val library by lazy { AssetTopicSource(context.assets, TopicParser()).load() }
    val clock = VariantClock.create(context)
    val today = Today(clock)
    val progressStore = ProgressStore(dataStore, clock)
    val resumeStore = ResumeStore(dataStore)
}
