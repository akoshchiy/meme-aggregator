package com.roguepnz.memeagg.source.cursor

import com.roguepnz.memeagg.metrics.MetricsService
import com.roguepnz.memeagg.source.ContentSource
import com.roguepnz.memeagg.source.model.RawContent
import com.roguepnz.memeagg.source.state.StateProvider
import com.roguepnz.memeagg.util.MetricChannel
import com.roguepnz.memeagg.util.Times
import com.roguepnz.memeagg.util.loggerFor
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.Tag
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.time.delay
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger


data class CursorState(
    var cursor: Cursor? = null,
    var crawledCount: Int = 0
)

class CursorContentSource(private val cursorProvider: CursorProvider,
                          private val stateProvider: StateProvider<CursorState>,
                          private val checkCount: Int,
                          private val updateDelaySec: Int,
                          private val metrics: MetricsService,
                          private val tag: String) : ContentSource {

    private val logger = loggerFor<CursorContentSource>()
    private val scope = CoroutineScope(Dispatchers.IO)

    private val startTime = Times.now()

    private val crawledCounter = Counter.builder("crawler.source.itemcount")
        .tag("type", tag)
        .description("total crawled items count")
        .register(metrics.registry)

    init {
        Gauge.builder("crawler.source.rate", this::getCrawlRate)
            .tag("type", tag)
            .description("source crawling rate in items per sec")
            .register(metrics.registry)
    }

    private fun getCrawlRate(): Double {
        val secs = Times.now() - startTime
        return crawledCounter.count() / secs
    }

    private fun buildChannel(): Channel<RawContent> {
        val metric = metrics.registry
            .gauge("crawler.source.queuesize", listOf(Tag.of("type", tag)), AtomicInteger(0))!!

        return MetricChannel(Channel(Channel.UNLIMITED), metric)
    }

    override fun listen(): ReceiveChannel<RawContent> {
        val channel = buildChannel()
        scope.launch {
            crawl(channel)
        }
        scope.launch { startUpdate(channel) }

        return channel
    }

    private suspend fun crawl(channel: Channel<RawContent>) {
        try {
            doCrawl(channel)
        } catch (e: Exception) {
            logger.error("crawl iteration failed", e)
            crawl(channel)
        }
    }

    private suspend fun doCrawl(channel: Channel<RawContent>) {
        val state = stateProvider.get() ?: CursorState()
        while (true) {
            val cursor = state.cursor
            val content = cursorProvider(cursor)

            if (content.data.isEmpty()) {
                break
            }

            content.data.forEach {
                crawledCounter.increment()
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
            try {
                checkUpdate(channel)
                delay(Duration.ofSeconds(updateDelaySec.toLong()))
            } catch (e: Exception) {
                logger.error("update iteration failed", e)
            }
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