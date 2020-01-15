package com.roguepnz.memeagg.source.cursor

import com.roguepnz.memeagg.source.ContentSource
import com.roguepnz.memeagg.source.model.RawContent
import com.roguepnz.memeagg.source.state.StateProvider
import com.roguepnz.memeagg.util.loggerFor
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.time.delay
import java.time.Duration


data class CursorState(
    var cursor: Cursor? = null,
    var crawledCount: Int = 0
)

class CursorContentSource(private val cursorProvider: CursorProvider,
                          private val stateProvider: StateProvider<CursorState>,
                          private val checkCount: Int) : ContentSource {

    private val logger = loggerFor<CursorContentSource>()

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun listen(): ReceiveChannel<RawContent> {
        val channel = Channel<RawContent>(Channel.UNLIMITED)

        scope.launch {
            try {
                startCrawl(channel)
            } catch (e: Exception) {
                logger.error("crawl iteration failed", e)
            }
        }
//        scope.launch { startUpdate(channel) }

        return channel
    }

    private suspend fun startCrawl(channel: Channel<RawContent>) {
        val state = stateProvider.get() ?: CursorState()
        while (true) {
            val cursor = state.cursor
            val content = cursorProvider(cursor)

            if (content.data.isEmpty()) {
                break
            }

            content.data.forEach {
                channel.send(it)
            }

            state.cursor = content.cursor
            state.crawledCount += content.data.size

            stateProvider.save(state)

            if (!content.cursor.hasNext) {
                break
            }
        }
    }

    private suspend fun startUpdate(channel: Channel<RawContent>) {
        while (true) {
            checkUpdate(channel)
            delay(Duration.ofMinutes(1))
        }
    }

    private suspend fun checkUpdate(channel: Channel<RawContent>) {
        val state = stateProvider.get()

        if (state == null || state.crawledCount < checkCount) {
            return
        }

        var count = 0
        var cursor: Cursor? = null

        while (count < checkCount) {
            val content = cursorProvider(cursor)

            count += content.data.size
            cursor = content.cursor

            content.data.forEach {
                channel.send(it)
            }

            if (!content.cursor.hasNext) {
                break
            }
        }
    }
}