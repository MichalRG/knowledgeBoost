package com.krzyzowski.knowledgeboost.time

import android.content.Context
import java.time.Duration

object VariantClock {
    fun create(context: Context): AppClock = DeviceClock { Duration.ZERO }
}
