package com.roguepnz.memeagg.feed.api

import com.roguepnz.memeagg.core.model.ContentPreview

data class Feed(val items: List<ContentPreview>, val after: String?) {
    val count: Int get() = items.size

}