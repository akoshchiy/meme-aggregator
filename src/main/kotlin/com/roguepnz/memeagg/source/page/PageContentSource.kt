package com.roguepnz.memeagg.source.page

import com.roguepnz.memeagg.source.ContentSource
import com.roguepnz.memeagg.source.debeste.DebesteContentSource
import com.roguepnz.memeagg.source.model.RawContent
import com.roguepnz.memeagg.source.state.StateProvider
import com.roguepnz.memeagg.util.UrlDownloader
import com.roguepnz.memeagg.util.loggerFor
import com.typesafe.config.Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import java.time.Duration



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
                        private val stateProvider: StateProvider<PageState>) : ContentSource {

    private val logger = loggerFor<DebesteContentSource>()
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun listen(): ReceiveChannel<RawContent> {
        val channel = Channel<RawContent>(Channel.UNLIMITED)
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
            .forEach { channel.send(it) }
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