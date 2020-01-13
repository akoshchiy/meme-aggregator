package com.roguepnz.memeagg.source

import com.roguepnz.memeagg.source.model.RawContent
import com.roguepnz.memeagg.source.model.RawMeta
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

interface ContentSource {
    fun listen(scope: CoroutineScope): ReceiveChannel<RawContent>
    fun stop()
}