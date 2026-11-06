package com.krzyzowski.knowledgeboost

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import com.krzyzowski.knowledgeboost.content.AssetTopicSource
import com.krzyzowski.knowledgeboost.content.DailyQuoteParser
import com.krzyzowski.knowledgeboost.content.DocumentTopicFolder
import com.krzyzowski.knowledgeboost.content.Library
import com.krzyzowski.knowledgeboost.content.TopicLibraryStore
import com.krzyzowski.knowledgeboost.content.TopicParser
import com.krzyzowski.knowledgeboost.data.ProgressStore
import com.krzyzowski.knowledgeboost.data.DailyQuoteStore
import com.krzyzowski.knowledgeboost.data.ResumeStore
import com.krzyzowski.knowledgeboost.data.TopicFolderStore
import com.krzyzowski.knowledgeboost.time.Today
import com.krzyzowski.knowledgeboost.time.VariantClock

class AppContainer(context: Context) {
    private val dataStore = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("knowledgeboost") },
    )

    val libraryStore = TopicLibraryStore(
        Library(
            AssetTopicSource(context.assets, TopicParser()).load().topics,
            DailyQuoteParser().parse(
                context.assets.open(DailyQuoteParser.FILE_NAME).bufferedReader(Charsets.UTF_8).use { it.readText() },
            ),
        ),
        TopicFolderStore(dataStore),
        DocumentTopicFolder(context.contentResolver),
    )
    val library = libraryStore.library
    val clock = VariantClock.create(context)
    val today = Today(clock)
    val progressStore = ProgressStore(dataStore, clock)
    val resumeStore = ResumeStore(dataStore)
    val dailyQuoteStore = DailyQuoteStore(dataStore)
}
