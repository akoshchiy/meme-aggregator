package com.roguepnz.memeagg.source.model

import com.roguepnz.memeagg.core.model.ContentType

data class Payload(val type: ContentType, val url: String) {
    val extension: String get() {
        val split = url.split(".")
        return split[split.size - 1]
    }
}