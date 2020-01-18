package com.roguepnz.memeagg.source.debeste

import com.roguepnz.memeagg.core.model.ContentType
import com.roguepnz.memeagg.util.UrlDownloader
import com.roguepnz.memeagg.source.ContentSource
import com.roguepnz.memeagg.source.model.Payload
import com.roguepnz.memeagg.source.model.RawContent
import com.roguepnz.memeagg.source.page.*
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
import java.util.regex.Pattern
import kotlin.Exception


private const val BASE_URL = "http://debeste.de"
private const val TIME_FORMAT = "yyyy-MM-dd hh:mm:ss"
private val TIME_RE = Pattern.compile("hinzugefügt: (\\d{4}-\\d{2}-\\d{2} \\d+:\\d+:\\d+)")


class DebesteContentSource(config: PageConfig,
                           stateProvider: StateProvider<PageState>,
                           private val downloader: UrlDownloader) : ContentSource {

    private val source = PageContentSource(
        config,
        this::getPageUrl,
        this::pagePage,
        downloader,
        stateProvider
    )

    override fun listen(): ReceiveChannel<RawContent> {
        return source.listen()
    }

    private fun getPageUrl(num: Int): String {
        return "${BASE_URL}/${num}"
    }

    private suspend fun pagePage(page: String): List<RawContent> {
        val doc = Jsoup.parse(page, BASE_URL)
        val content = doc.selectFirst("#content")
        val boxes = content.select("div.box")

        return boxes.map {
            parseBox(it)
        }
    }

    private suspend fun parseBox(html: Element): RawContent {
        val postUrl = html.selectFirst(".objectMeta > a[href^=$BASE_URL]").attr("href").trim()
        val postHtml = downloader.downloadString(postUrl)
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

        val time = extractTime(postHtml)

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

    private fun extractTime(html: String): Int {
        val matcher = TIME_RE.matcher(html)
        return if (matcher.find()) (SimpleDateFormat(TIME_FORMAT).parse(matcher.group(1)).time / 1000).toInt() else 0
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