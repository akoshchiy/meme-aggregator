package com.roguepnz.memeagg.core.model

import com.roguepnz.memeagg.source.config.SourceType

data class ContentView(
    val id: String,
    val rawId: String,
    val contentType: ContentType,
    val url: String,
    val hash: String,
    val sourceType: SourceType,
    val sourceId: String,
    val publishTime: Int,
    val likesCount: Int,
    val dislikesCount: Int,
    val commentsCount: Int,
    val rating: Int,
    val author: String,
    val title: String
)