package com.roguepnz.memeagg.source.model

import com.roguepnz.memeagg.model.ContentType

data class RawContent(val id: String,
                      val title: String,
                      val payload: Payload,
                      val publishTime: Int,
                      val likesCount: Int,
                      val dislikesCount: Int,
                      val commentsCount: Int) {
}