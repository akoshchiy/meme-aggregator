package com.roguepnz.memeagg.source.ngag.group

import com.roguepnz.memeagg.source.ContentSource
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
                             private val stateProvider: StateProvider<NGagGroupState>) : ContentSource {

    private val contentChannel: Channel<RawContent> = Channel(config.bufferSize)
    private val metaUpdateChannel: Channel<RawMeta> = Channel(config.bufferSize)

    override suspend fun start() {
        startCrawling()
    }

    override fun contentChannel(): ReceiveChannel<RawContent> {
        return contentChannel
    }

    override fun metaChannel(): ReceiveChannel<RawMeta> {
        return metaUpdateChannel
    }

    private suspend fun startCrawling() {
        val state = stateProvider.getOrDefault(NGagGroupState::class) { NGagGroupState() }
        while (true) {
            val res = client.getByGroup(config.group, state.cursor)
            if (res.cursor == null) {
                break
            }
            res.content.forEach {
                contentChannel.send(it)
            }
            state.cursor = res.cursor
            stateProvider.save(state)
        }
    }
}