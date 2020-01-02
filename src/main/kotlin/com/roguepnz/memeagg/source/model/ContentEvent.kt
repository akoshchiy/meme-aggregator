package com.roguepnz.memeagg.source.model

import com.roguepnz.memeagg.model.Content
import com.roguepnz.memeagg.model.Metadata

data class ContentEvent(val content: Content, val metadata: Metadata)