package com.roguepnz.memeagg.source.ngag.group

import com.roguepnz.memeagg.source.ContentSource
import com.roguepnz.memeagg.source.cursor.*
import com.roguepnz.memeagg.source.model.RawContent
import com.roguepnz.memeagg.source.model.RawMeta
import com.roguepnz.memeagg.source.ngag.NGagClient
import com.roguepnz.memeagg.source.state.StateProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch

class NGagGroupContentSource(private val config: NGagGroupConfig,
                             private val client: NGagClient,
                             stateProvider: StateProvider<CursorState>) : ContentSource {

    private val cursorContentSource = CursorContentSource(
        prepareProvider(),
        stateProvider,
        config.bufferSize,
        300
    )

    private fun prepareProvider(): CursorProvider {
        return {
            val res = client.getByGroup(config.group, it?.cursor ?: "")
            CursorContent(
                Cursor(res.cursor ?: "", res.cursor != null),
                res.content
            )
        }
    }


    override fun listen(scope: CoroutineScope): ReceiveChannel<RawContent> {
        return cursorContentSource.listen(scope)
    }

    override fun stop() {
        cursorContentSource.stop()
    }
}