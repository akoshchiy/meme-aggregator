package com.roguepnz.meme.model

data class Metadata(
    val contentId: String,
    val publishTime: Int,
    val likesCount: Int,
    val dislikesCount: Int,
    val commentsCount: Int
)