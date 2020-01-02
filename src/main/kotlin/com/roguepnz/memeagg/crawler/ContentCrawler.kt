package com.roguepnz.memeagg.crawler

import com.roguepnz.memeagg.source.ContentSource
import com.roguepnz.memeagg.source.ContentSourceLoader
import com.roguepnz.memeagg.util.JSON
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ContentCrawler(private val loader: ContentSourceLoader) {

    fun start() {
        loader.getSources().forEach { listenSource(it) }
    }

    private fun listenSource(source: ContentSource) {
        GlobalScope.launch(Dispatchers.IO) {
            val channel = source.listen()
            while (true) {
                val content = channel.receive()
                // TODO save to db
                println(JSON.stringify(content))
            }
        }
    }
}