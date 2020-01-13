package com.roguepnz.memeagg.source.ngag.tag

import com.roguepnz.memeagg.source.ContentSource
import com.roguepnz.memeagg.source.model.RawContent
import com.roguepnz.memeagg.source.model.RawMeta
import com.roguepnz.memeagg.source.ngag.NGagClient
import com.roguepnz.memeagg.source.state.StateProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch

class NGagTagContentSource(private val config: NGagTagConfig,
                           private val client: NGagClient,
                           private val stateProvider: StateProvider<NGagTagState>) : ContentSource {

    private val contentChannel: Channel<RawContent> = Channel(config.bufferSize)
    private val metaUpdateChannel: Channel<RawMeta> = Channel(config.bufferSize)

    override fun start() {
        GlobalScope.launch(Dispatchers.IO) {
            // TODO update
            // TODO it can be parallel
            startCrawling()
        }
    }

    override fun contentChannel(): ReceiveChannel<RawContent> {
        return contentChannel
    }

    override fun metaChannel(): ReceiveChannel<RawMeta> {
        return metaUpdateChannel
    }

    private suspend fun startCrawling() {
        val state = stateProvider.getOrDefault(NGagTagState::class) { NGagTagState() }
        while (true) {
            val res = client.getByTag(config.tag, state.cursor)
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