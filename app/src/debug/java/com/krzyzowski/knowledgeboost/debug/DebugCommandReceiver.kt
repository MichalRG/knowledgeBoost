package com.krzyzowski.knowledgeboost.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.krzyzowski.knowledgeboost.KnowledgeBoostApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DebugCommandReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val container = (context.applicationContext as KnowledgeBoostApp).container
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                pending.resultData = when (intent.action) {
                    SET_TIME_OFFSET -> {
                        val minutes = intent.getIntExtra("minutes", 0).toLong()
                        DebugTimeOffset(context).set(minutes)
                        container.today.refresh()
                        "offset_minutes=$minutes app_now=${container.clock.now()}"
                    }
                    else -> "unknown action ${intent.action}"
                }
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        const val SET_TIME_OFFSET = "com.krzyzowski.knowledgeboost.debug.SET_TIME_OFFSET"
    }
}
