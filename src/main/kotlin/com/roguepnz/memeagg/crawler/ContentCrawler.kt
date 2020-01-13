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
        GlobalScope.launch(Dispatchers.IO) {
            val channel = source.listen(this)
            for (raw in channel) {
                launch {
                    processContent(id, raw)
                }
            }
//            (0..10).forEach {
//                launch {
//                    for (raw in channel) {
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

        writer.add(
            Content(
                null,
                sourceId,
                raw.id,
                raw.payload.type.code,
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