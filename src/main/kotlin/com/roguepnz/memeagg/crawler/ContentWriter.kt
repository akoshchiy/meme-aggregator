package com.roguepnz.memeagg.crawler

import com.roguepnz.memeagg.core.dao.ContentDao
import com.roguepnz.memeagg.core.model.Content
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select

class ContentWriter(private val config: CrawlerConfig, private val dao: ContentDao) {

    private val channel: Channel<Content> = Channel()
    private val batch: MutableList<Content> = ArrayList()

    fun start() {
        GlobalScope.launch {
            loop()
        }
    }

    suspend fun add(content: Content) {
        channel.send(content)
    }

    private suspend fun loop() {
        var deadline = 0L
        while (true) {
            val remainingTime = deadline - System.currentTimeMillis()

            if (batch.isNotEmpty() && remainingTime <= 0 || batch.size >= config.writerQueueSize) {
                dao.insert(batch)
                batch.clear()
                continue
            }

            select<Unit> {
                channel.onReceive {
                    batch.add(it)
                    if (batch.size == 1) {
                        deadline = System.currentTimeMillis() + config.writerWaitTimeSec * 1000L
                    }
                }
                if (batch.isNotEmpty()) {
                    onTimeout(remainingTime){}
                }
            }
        }
    }
}