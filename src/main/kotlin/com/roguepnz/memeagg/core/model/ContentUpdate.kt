package com.roguepnz.memeagg.core.model

data class ContentUpdate(val rawId: String,
                         val likesCount: Int,
                         val dislikesCount: Int,
                         val commentsCount: Int,
                         val rating: Int)