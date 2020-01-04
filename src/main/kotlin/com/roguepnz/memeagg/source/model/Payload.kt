package com.roguepnz.memeagg.source.model

import com.roguepnz.memeagg.core.model.ContentType

data class Payload(val type: ContentType, val url: String)