package com.krzyzowski.knowledgeboost.ui

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList

sealed interface Destination {
    val route: String

    data object Home : Destination {
        override val route = "home"
    }

    data class Category(val name: String) : Destination {
        override val route = "category:$name"
    }

    data class Topic(val id: String) : Destination {
        override val route = "topic:$id"
    }

    data object Missed : Destination {
        override val route = "missed"
    }

    data class MissedCard(val startId: String) : Destination {
        override val route = "missed_card:$startId"
    }

    data object Removed : Destination {
        override val route = "removed"
    }

    companion object {
        val stackSaver: Saver<SnapshotStateList<Destination>, Any> = listSaver(
            save = { stack -> stack.map { it.route } },
            restore = { routes ->
                routes.map { route ->
                    val argument = route.substringAfter(':', "")
                    when (route.substringBefore(':')) {
                        "category" -> Category(argument)
                        "topic" -> Topic(argument)
                        "missed" -> Missed
                        "missed_card" -> MissedCard(argument)
                        "removed" -> Removed
                        else -> Home
                    }
                }.toMutableStateList()
            },
        )
    }
}
