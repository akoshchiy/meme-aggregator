package com.roguepnz.memeagg.source.model


data class RawContent(val id: String,
                      val title: String,
                      val author: String,
                      val payload: Payload,
                      val publishTime: Int,
                      val likesCount: Int,
                      val dislikesCount: Int,
                      val rating: Int,
                      val commentsCount: Int)