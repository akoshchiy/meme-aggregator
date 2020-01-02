package com.roguepnz.memeagg.source

import com.roguepnz.memeagg.source.model.RawContent
import kotlinx.coroutines.channels.ReceiveChannel


interface ContentSource {
    fun listen(): ReceiveChannel<RawContent>
}