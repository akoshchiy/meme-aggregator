package com.roguepnz.memeagg.feed.api

import com.roguepnz.memeagg.core.model.ContentPreview

data class Feed(val items: List<ContentPreview>) {
    val count: Int get() = items.size
    val nextAfter: String?
        get() = if (items.isEmpty()) null else items[items.size - 1].id
}