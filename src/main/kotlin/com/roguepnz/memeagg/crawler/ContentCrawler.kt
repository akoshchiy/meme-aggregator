package com.roguepnz.memeagg.crawler

import com.roguepnz.memeagg.core.model.Content
import com.roguepnz.memeagg.core.model.Meta
import com.roguepnz.memeagg.source.ContentSource
import com.roguepnz.memeagg.source.ContentSourceBuilder
import com.roguepnz.memeagg.util.JSON
import com.typesafe.config.Config
import kotlinx.coroutines.*

class ContentCrawler(config: CrawlerConfig, private val writer: ContentWriter) {

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

    fun crawl(source: ContentSource) {
        source.start()
        GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                val raw = source.contentChannel().receive()
                // TODO save to db
                println(JSON.stringify(raw))
                writer.add(
                    Content(
                        raw.id,
                        raw.payload.type,
                        raw.payload.url,
                        "",
                        Meta(raw.publishTime, raw.likesCount, raw.dislikesCount, raw.commentsCount)
                    )
                )
            }
        }
    }
}