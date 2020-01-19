package com.roguepnz.memeagg.source.reddit

import com.roguepnz.memeagg.metrics.MetricsService
import com.roguepnz.memeagg.source.ContentSource
import com.roguepnz.memeagg.source.cursor.*
import com.roguepnz.memeagg.source.model.RawContent
import com.roguepnz.memeagg.source.state.StateProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

class RedditContentSource(private val config: RedditConfig,
                          private val client: RedditClient,
                          stateProvider: StateProvider<CursorState>,
                          metrics: MetricsService) : ContentSource {

    private val source = CursorContentSource(
        cursorProvider(),
        stateProvider,
        config.lastUpdateCount,
        config.updateDelaySec,
        metrics,
        "reddit"
    )

    private fun cursorProvider(): CursorProvider {
        return {
            val res = client.getContent(config.subreddits, it?.cursor)
            CursorContent(
                Cursor(res.after ?: "", res.after != null),
                res.data
            )
        }
    }

    override fun listen(): ReceiveChannel<RawContent> {
        return source.listen()
    }
}