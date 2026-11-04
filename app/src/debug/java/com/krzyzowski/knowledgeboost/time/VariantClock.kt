package com.krzyzowski.knowledgeboost.time

import android.content.Context
import com.krzyzowski.knowledgeboost.debug.DebugTimeOffset

object VariantClock {
    fun create(context: Context): AppClock = DeviceClock(DebugTimeOffset(context)::current)
}
