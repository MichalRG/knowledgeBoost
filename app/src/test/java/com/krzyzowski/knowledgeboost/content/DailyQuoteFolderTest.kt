package com.krzyzowski.knowledgeboost.content

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.krzyzowski.knowledgeboost.data.TopicFolderStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class DailyQuoteFolderTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val dataStore by lazy {
        PreferenceDataStoreFactory.create(scope = scope) { File(folder.root, "folder.preferences_pb") }
    }
    private val bundled = Library(listOf(ContentBuilder.topic("bundled", listOf("known"))), listOf(ContentBuilder.quote()))
    private val source = FakeTopicFolder()
    private val store by lazy { TopicLibraryStore(bundled, TopicFolderStore(dataStore), source) }

    @After
    fun closeDataStore() {
        scope.cancel()
    }

    @Test
    fun `the reserved file replaces quotes without becoming a topic and quote-only edits are published`() = runTest {
        source.folders["folder"] = listOf(TopicFileBuilder.file(), ContentBuilder.quoteFile("First", "Second"))
        store.select("folder")
        assertEquals(listOf("First", "Second"), store.library.value.quotes.map { it.text })
        assertEquals(1, store.state.value.topics)
        assertEquals(1, store.state.value.questions)
        val topics = store.library.value.topics
        source.folders["folder"] = listOf(TopicFileBuilder.file(), ContentBuilder.quoteFile("Edited"))

        store.reload()

        assertEquals(listOf("Edited"), store.library.value.quotes.map { it.text })
        assertEquals(topics, store.library.value.topics)
        assertNull(store.state.value.error)
    }

    @Test
    fun `invalid quote files reject topic changes too and preserve both last good lists`() = runTest {
        source.folders["folder"] = listOf(TopicFileBuilder.file(), ContentBuilder.quoteFile("Good"))
        store.select("folder")
        val before = store.library.value
        source.folders["folder"] = listOf(TopicFileBuilder.file("new"), ContentBuilder.quoteFile())

        store.reload()

        assertSame(before, store.library.value)
        assertTrue(store.state.value.error.orEmpty().contains(DailyQuoteParser.FILE_NAME))
        assertEquals(listOf("Good"), store.library.value.quotes.map { it.text })
    }

    @Test
    fun `removing the reserved file and disconnecting restore the bundled daily thoughts`() = runTest {
        source.folders["folder"] = listOf(TopicFileBuilder.file(), ContentBuilder.quoteFile("Custom"))
        store.select("folder")
        source.folders["folder"] = listOf(TopicFileBuilder.file())
        store.reload()
        assertEquals(bundled.quotes, store.library.value.quotes)
        assertEquals(1, store.state.value.topics)

        store.disconnect()

        assertEquals(bundled.quotes, store.library.value.quotes)
        assertEquals(bundled.topics, store.library.value.topics)
    }

    @Test
    fun `two files with the reserved name are rejected rather than choosing an arbitrary list`() {
        val file = ContentBuilder.quoteFile("Thought")
        assertThrows(IllegalArgumentException::class.java) {
            TopicLibraryMerger().merge(bundled, listOf(file, file))
        }
    }
}
