package com.roguepnz.memeagg.core.model

import org.bson.codecs.pojo.annotations.BsonId

data class Content(@BsonId val id: String?,
                   val contentType: Int,
                   val url: String,
                   val hash: String,
                   val meta: Meta)

