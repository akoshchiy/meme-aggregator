package com.roguepnz.memeagg.source.debeste

import com.roguepnz.memeagg.core.model.ContentType
import com.roguepnz.memeagg.util.UrlDownloader
import com.roguepnz.memeagg.source.ContentSource
import com.roguepnz.memeagg.source.model.Payload
import com.roguepnz.memeagg.source.model.RawContent
import com.roguepnz.memeagg.source.state.StateProvider
import com.roguepnz.memeagg.util.loggerFor
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.time.delay
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.text.SimpleDateFormat
import java.time.Duration
import kotlin.Exception


private const val BASE_URL = "http://debeste.de"
private val DATE_PATTERN = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")

class DebesteContentSource(private val config: DebesteConfig,
                           private val stateProvider: StateProvider<DebesteState>,
                           private val downloader: UrlDownloader) : ContentSource {

    private val logger = loggerFor<DebesteContentSource>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

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
        val state = stateProvider.get() ?: DebesteState(1)

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

    private suspend fun load(url: String): String {
        val result = downloader.download(url)
        return String(result.data)
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
        val url = "${BASE_URL}/$num"
        val page = load(url)

        val doc = Jsoup.parse(page, BASE_URL)
        val content = doc.selectFirst("#content")
        val boxes = content.select("div.box")

        return boxes.map {
            parseBox(it)
        }
    }

    private suspend fun parseBox(html: Element): RawContent {
        val postUrl = html.selectFirst(".objectMeta > a[href^=$BASE_URL]").attr("href").trim()
        val postHtml = load(postUrl)
        val post = Jsoup.parse(postHtml, BASE_URL)

        val title = post.selectFirst("h2").text()

        val wrapper = post.selectFirst(".objectWrapper")
        val payload = extractPayload(wrapper)

        val rateStr = post.selectFirst(".rate").text()
        val rate = rateStr.substring(1, rateStr.length - 1).toInt()

        val objectMeta = post.selectFirst(".objectMeta")
        val commentText = objectMeta.selectFirst("a[href^=javascript]").text().trim()
        val commentPart = commentText.split("(")[1]
        val comments = commentPart.substring(0, commentPart.length - 1).toInt()

        val time = 0

        val id = postUrl.split("/")[3]

        return RawContent(
            id,
            title,
            "",
            payload,
            time,
            0,
            0,
            rate,
            comments
        )
    }

    private fun extractPayload(wrapper: Element): Payload {
        val video = wrapper.selectFirst("video")
        if (video != null) {
            val videoUrl = video.selectFirst("source").attr("src")
            return Payload(ContentType.VIDEO, "${BASE_URL}$videoUrl")
        }
        val img = wrapper
            .selectFirst("img")
            .attr("src")

        val imgUrl = "${BASE_URL}$img"

        return Payload(ContentType.IMAGE, imgUrl)
    }
}