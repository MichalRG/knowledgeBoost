package com.krzyzowski.knowledgeboost.content

import com.krzyzowski.knowledgeboost.data.TopicFolderStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException

class TopicLibraryStore(
    val bundled: Library,
    private val settings: TopicFolderStore,
    private val source: TopicFolderSource,
    private val io: CoroutineDispatcher = Dispatchers.IO,
) {
    private val mutex = Mutex()
    private val merger = TopicLibraryMerger()
    private val currentLibrary = MutableStateFlow(bundled)
    private val currentState = MutableStateFlow(TopicFolderState())

    val library = currentLibrary.asStateFlow()
    val state = currentState.asStateFlow()

    suspend fun reload() = update {
        val uri = settings.uri.first()
        currentState.value = currentState.value.copy(uri = uri)
        publish(uri, uri?.let { load(it) } ?: bundled)
    }

    suspend fun select(uri: String) = update {
        val library = load(uri)
        val previous = settings.uri.first()
        withContext(io) { source.retain(uri) }
        settings.set(uri)
        publish(uri, library)
        if (previous != null && previous != uri) withContext(io) { source.release(previous) }
    }

    suspend fun disconnect() = update {
        val previous = settings.uri.first()
        settings.set(null)
        publish(null, bundled)
        if (previous != null) withContext(io) { source.release(previous) }
    }

    private suspend fun load(uri: String): Library = withContext(io) {
        merger.merge(bundled, source.read(uri))
    }

    private fun publish(uri: String?, library: Library) {
        if (currentLibrary.value.topics != library.topics) currentLibrary.value = library
        currentState.value = currentState.value.copy(
            uri = uri,
            topics = library.topics.size - bundled.topics.size,
            questions = library.questions.size - bundled.questions.size,
        )
    }

    private suspend fun update(action: suspend () -> Unit) = mutex.withLock {
        currentState.value = currentState.value.copy(loading = true, error = null)
        try {
            action()
        } catch (error: IOException) {
            currentState.value = currentState.value.copy(error = error.message ?: error.javaClass.simpleName)
        } catch (error: SecurityException) {
            currentState.value = currentState.value.copy(error = error.message ?: error.javaClass.simpleName)
        } catch (error: IllegalArgumentException) {
            currentState.value = currentState.value.copy(error = error.message ?: error.javaClass.simpleName)
        } finally {
            currentState.value = currentState.value.copy(loading = false)
        }
    }
}
