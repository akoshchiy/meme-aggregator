package com.roguepnz.memeagg.source.orschlurch

import com.roguepnz.memeagg.core.model.ContentType
import com.roguepnz.memeagg.metrics.MetricsService
import com.roguepnz.memeagg.source.ContentSource
import com.roguepnz.memeagg.source.model.Payload
import com.roguepnz.memeagg.source.model.RawContent
import com.roguepnz.memeagg.source.page.*
import com.roguepnz.memeagg.source.state.StateProvider
import com.roguepnz.memeagg.util.UrlDownloader
import kotlinx.coroutines.channels.ReceiveChannel
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.text.SimpleDateFormat


private const val TIME_FORMAT = "dd.MM.yyyy"

class OrschlurchContentSource(config: PageConfig,
                              stateProvider: StateProvider<PageState>,
                              metrics: MetricsService,
                              private val downloader: UrlDownloader) : ContentSource {

    private val source = PageContentSource(
        config,
        this::getPageUrl,
        this::parsePage,
        downloader,
        metrics,
        "orschlurch",
        stateProvider
    )

    override fun listen(): ReceiveChannel<RawContent> {
        return source.listen()
    }

    private fun getPageUrl(num: Int): String {
        return "https://de.orschlurch.net/seite/$num"
    }

    private suspend fun parsePage(page: String): List<RawContent> {
        val doc = Jsoup.parse(page, "https://de.orschlurch.net")
        val content = doc.selectFirst(".main-content")
        val cards  = content.select("div.h-100")
        return cards.flatMap {
            parseCard(it)
        }
    }

    private suspend fun parseCard(card: Element): List<RawContent> {
        val postUrl = card.selectFirst("a").attr("href")

        val type = card.selectFirst("p.card-cat > a").text()

        val text = card.selectFirst(".card-text").text()
        val spans = card.select(".card-meta > span")
        val date = spans[0].text()
        val likes = spans[1].text()
        val comments = spans[2].text()

        val id = postUrl.split("/")[2]

        val payloads = extractPayload(type, postUrl)

        return payloads.mapIndexed { idx, p ->
            RawContent(
                "${id}-${idx}",
                text,
                "",
                p,
                (SimpleDateFormat(TIME_FORMAT).parse(date).time / 1000).toInt() + idx,
                likes.toInt(),
                0,
                0,
                comments.toInt()
            )
        }
    }

    private suspend fun extractPayload(type: String, url: String): List<Payload> {
        if (type == "Lustige Artikel") {
            return ArrayList()
        }

        val post = Jsoup.parse(downloader.downloadString("https://de.orschlurch.net/$url"), "https://de.orschlurch.net")
        if (type == "Videos") {
            val video = extractVideo(post) ?: return ArrayList()
            return listOf(Payload(ContentType.VIDEO, video))
        }

        if (type == "Pix") {
            return extractPics(post).map {
                Payload(ContentType.IMAGE, it)
            }
        }

        return ArrayList()
    }

    private fun extractPics(post: Element): List<String> {
        val payloads = post.select("div.container > div.row > div.col-md-12 > img")
        return payloads.map {
            it.attr("src")
        }
    }

    private fun extractVideo(post: Element): String? {
        val source = post.selectFirst("video > source")
        return source?.attr("src")
    }
}