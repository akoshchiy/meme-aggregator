package com.roguepnz.memeagg.source.model


data class RawContent(val id: String,
                      val title: String,
                      val payload: Payload,
                      val publishTime: Int,
                      val likesCount: Int,
                      val dislikesCount: Int,
                      val commentsCount: Int) {
}