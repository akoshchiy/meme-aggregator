package com.roguepnz.memeagg.source

import com.roguepnz.memeagg.source.model.RawContent
import com.roguepnz.memeagg.source.model.RawMetadata
import kotlinx.coroutines.channels.ReceiveChannel

interface ContentSource {
    fun start()
    fun contentChannel(): ReceiveChannel<RawContent>
    fun metaUpdateChannel(): ReceiveChannel<RawMetadata>
}