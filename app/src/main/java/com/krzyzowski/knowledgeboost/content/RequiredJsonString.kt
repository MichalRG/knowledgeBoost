package com.krzyzowski.knowledgeboost.content

import org.json.JSONObject

internal fun JSONObject.requiredString(where: String, key: String): String {
    val value = opt(key)
    require(value is String && value.isNotBlank()) { "$where: $key must be a non-blank string" }
    return value
}
