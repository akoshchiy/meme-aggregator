package com.roguepnz.memeagg.crawler

import com.roguepnz.memeagg.source.ContentSource
import com.roguepnz.memeagg.source.ContentSourceBuilder
import com.roguepnz.memeagg.util.JSON
import com.typesafe.config.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ContentCrawler(config: Config, private val builder: ContentSourceBuilder) {

    fun start() {
        // TODO distributed
        // TODO async payload downloader
        // TODO content duplicate detect
        builder.buildSources(builder.sources).forEach {
            listenSource(it)
        }
    }

    private fun listenSource(source: ContentSource) {
        source.start()
        GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                val content = source.contentChannel().receive()
                // TODO save to db
                println(JSON.stringify(content))
            }
        }
    }
}