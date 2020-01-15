package com.roguepnz.memeagg.source.ngag.tag

import com.roguepnz.memeagg.source.ContentSource
import com.roguepnz.memeagg.source.cursor.*
import com.roguepnz.memeagg.source.model.RawContent
import com.roguepnz.memeagg.source.ngag.NGagClient
import com.roguepnz.memeagg.source.state.StateProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

class NGagTagContentSource(private val config: NGagTagConfig,
                           private val client: NGagClient,
                           stateProvider: StateProvider<CursorState>) : ContentSource {

    private val cursorContentSource = CursorContentSource(
        cursorProvider(),
        stateProvider,
        300
    )

    private fun cursorProvider(): CursorProvider {
        return {
            val res = client.getByTag(config.tag, it?.cursor ?: "")
            CursorContent(
                Cursor(res.cursor ?: "", res.cursor != null),
                res.content
            )
        }
    }

    override fun listen(): ReceiveChannel<RawContent> {
        return cursorContentSource.listen()
    }
}