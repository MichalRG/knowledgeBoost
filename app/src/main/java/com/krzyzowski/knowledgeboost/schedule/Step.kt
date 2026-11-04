package com.krzyzowski.knowledgeboost.schedule

enum class Step(val code: String, val days: Int) {
    DAYS_7("7", 7),
    DAYS_30("30", 30),
    DAYS_90("90", 90),
    DAYS_180("180", 180),
    DAYS_360("360", 360);

    val next: Step
        get() = entries.getOrElse(ordinal + 1) { DAYS_360 }

    companion object {
        fun fromCode(code: String): Step = entries.first { it.code == code }
    }
}
