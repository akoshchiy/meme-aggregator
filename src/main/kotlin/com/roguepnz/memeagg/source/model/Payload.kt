package com.roguepnz.memeagg.source.model

import com.roguepnz.memeagg.model.ContentType

data class Payload(val type: ContentType, val url: String)