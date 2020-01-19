package com.roguepnz.memeagg.source.page

import com.roguepnz.memeagg.metrics.MetricsService
import com.roguepnz.memeagg.source.ContentSource
import com.roguepnz.memeagg.source.debeste.DebesteContentSource
import com.roguepnz.memeagg.source.model.RawContent
import com.roguepnz.memeagg.source.state.StateProvider
import com.roguepnz.memeagg.util.MetricChannel
import com.roguepnz.memeagg.util.Times
import com.roguepnz.memeagg.util.UrlDownloader
import com.roguepnz.memeagg.util.loggerFor
import com.typesafe.config.Config
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.Tag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger


typealias PageParser = suspend (page: String) -> List<RawContent>
typealias PageUrlProvider = (page: Int) -> String

data class PageState(var pages: Int)

class PageConfig(config: Config) {
    val maxPages = config.getInt("maxPages")
    val maxConcurrentDownloads = config.getInt("maxConcurrentDownloads")
    val updatePages = config.getInt("updatePages")
    val updateDelaySec = config.getInt("updateDelaySec")
}

class PageContentSource(private val config: PageConfig,
                        private val urlProvider: PageUrlProvider,
                        private val parser: PageParser,
                        private val downloader: UrlDownloader,
                        private val metrics: MetricsService,
                        private val tag: String,
                        private val stateProvider: StateProvider<PageState>) : ContentSource {

    private val logger = loggerFor<PageContentSource>()
    private val scope = CoroutineScope(Dispatchers.IO)

    private val startTime = Times.now()

    private val crawledCounter = Counter.builder("crawler.source.itemcount")
        .description("total crawled items count")
        .tag("type", tag)
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
        scope.launch { crawl(channel) }
        scope.launch { update(channel) }
        return channel
    }

    private suspend fun crawl(channel: Channel<RawContent>) {
        try {
            doCrawl(channel)
        } catch (e: Exception) {
            logger.error("crawl failed", e)
            crawl(channel)
        }
    }

    private suspend fun doCrawl(channel: Channel<RawContent>) {
        val state = stateProvider.get() ?: PageState(1)

        val step = config.maxConcurrentDownloads

        for (i in state.pages..config.maxPages step step) {
            download((i..(i+step)), channel)
            state.pages = i
            stateProvider.save(state)
        }
    }

    private suspend fun update(channel: Channel<RawContent>) {
        while (true) {
            delay(Duration.ofSeconds(config.updateDelaySec.toLong()))
            try {
                download((1..config.updatePages), channel)
            } catch (e: Exception) {
                logger.error("update iteration failed", e)
            }
        }
    }

    private suspend fun download(range: IntRange, channel: Channel<RawContent>) {
        range
            .map { scope.async { downloadPage(it) } }
            .map { it.await() }
            .flatMap { it.asIterable() }
            .forEach {
                channel.send(it)
                crawledCounter.increment()
            }
    }

    private suspend fun downloadPage(num: Int): List<RawContent> {
        return try {
            doDownload(num)
        } catch (e: Exception) {
            logger.error("download page failed $num", e)
            ArrayList()
        }
    }

    private suspend fun doDownload(num: Int): List<RawContent> {
        val url = urlProvider(num)
        val page = downloader.downloadString(url)

        return parser(page)
    }
}