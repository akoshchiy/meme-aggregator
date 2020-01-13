package com.roguepnz.memeagg.crawler

import com.roguepnz.memeagg.core.model.Content
import com.roguepnz.memeagg.core.model.Meta
import com.roguepnz.memeagg.crawler.payload.PayloadUploader
import com.roguepnz.memeagg.source.ContentSource
import com.roguepnz.memeagg.source.model.RawContent
import com.roguepnz.memeagg.util.Hashes
import com.roguepnz.memeagg.util.JSON
import com.roguepnz.memeagg.util.loggerFor
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ContentCrawler(private val writer: ContentWriter,
                     private val httpClient: HttpClient,
                     private val uploader: PayloadUploader) {

    private val logger = loggerFor<ContentCrawler>()

//    fun start() {
//        // TODO distributed
//        // TODO async payload downloader
//        // TODO content duplicate detect
//        writer.start()
//
////        builder.buildSources(builder.sources).forEach {
////            listenSource(it)
////        }
//    }

    fun crawl(id: String, source: ContentSource) {
//        val ctx = newFixedThreadPoolContext(10, "source-worker")
        source.start()
        GlobalScope.launch(Dispatchers.IO) {
            (0..10).forEach {
                launch {
                    for (raw in source.contentChannel()) {
                        processContent(id, raw)
                    }
                }
            }
//            for (raw in source.contentChannel()) {
//                launch {
//                    processContent(id, raw)
//                }
//            }
//            val scope = this
//            while (true) {
//                val raw = source.contentChannel().receive()
//                processContent(id, raw)
//
//                async {
//                }
//            }
//            (1..10).forEach { _ ->
//                GlobalScope.launch {
//                    for (raw in source.contentChannel()) {
//                        processContent(id, raw)
//                    }
//                }
//            }
        }
    }

    private suspend fun processContent(sourceId: String, raw: RawContent) {
        val resp = httpClient.get<HttpResponse>(raw.payload.url)
        val contentType = resp.headers["content-type"]
        val bytes = resp.readBytes()
        val hash = Hashes.md5(bytes)
        val key = "${sourceId}_${raw.id}"

        val url = uploader.upload(key, bytes, contentType!!)

        // TODO add hash check

        writer.add(
            Content(
                key,
                raw.payload.type,
                url,
                hash,
                raw.publishTime,
                raw.likesCount,
                raw.dislikesCount,
                raw.commentsCount,
                0
            )
        )

        logger.info(JSON.stringify(raw))
    }
}