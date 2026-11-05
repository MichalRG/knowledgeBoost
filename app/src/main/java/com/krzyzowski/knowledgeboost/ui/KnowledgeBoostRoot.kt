package com.krzyzowski.knowledgeboost.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.krzyzowski.knowledgeboost.content.TopicLibraryStore
import com.krzyzowski.knowledgeboost.time.Today
import com.krzyzowski.knowledgeboost.ui.category.CategoryRoute
import com.krzyzowski.knowledgeboost.ui.home.HomeRoute
import com.krzyzowski.knowledgeboost.ui.folder.TopicFolderRoute
import com.krzyzowski.knowledgeboost.ui.missed.MissedCardRoute
import com.krzyzowski.knowledgeboost.ui.missed.MissedRoute
import com.krzyzowski.knowledgeboost.ui.removed.RemovedRoute
import com.krzyzowski.knowledgeboost.ui.topic.TopicRoute
import kotlinx.coroutines.delay

@Composable
fun KnowledgeBoostRoot(today: Today, libraryStore: TopicLibraryStore) {
    val stack = rememberSaveable(saver = Destination.stackSaver) { mutableStateListOf<Destination>(Destination.Home) }
    val open: (Destination) -> Unit = { stack.add(it) }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            libraryStore.reload()
            while (true) {
                today.refresh()
                delay(15_000)
            }
        }
    }

    BackHandler(enabled = stack.size > 1) { stack.removeAt(stack.lastIndex) }

    Box(modifier = Modifier.semantics { testTagsAsResourceId = true }) {
        when (val destination = stack.last()) {
            Destination.Home -> HomeRoute(onOpen = open)
            is Destination.Category -> CategoryRoute(destination.name, onOpenTopic = { open(Destination.Topic(it)) })
            is Destination.Topic -> TopicRoute(destination.id)
            Destination.Missed -> MissedRoute(onOpenCard = { open(Destination.MissedCard(it)) })
            is Destination.MissedCard -> MissedCardRoute(destination.startId)
            Destination.Removed -> RemovedRoute()
            Destination.TopicFolder -> TopicFolderRoute()
        }
    }
}
