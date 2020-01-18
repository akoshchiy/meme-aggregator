package com.roguepnz.memeagg.crawler

import com.roguepnz.memeagg.core.dao.ContentDao
import com.roguepnz.memeagg.core.model.Content
import com.roguepnz.memeagg.core.model.ContentUpdate
import com.roguepnz.memeagg.util.BatchWorker

class ContentWriter(config: CrawlerConfig, dao: ContentDao) {

    private val saveWorker = BatchWorker<Content>(
        config.writerQueueSize,
        config.writerWaitTimeSec
    ) { dao.insert(it) }

    private val updateWorker = BatchWorker<ContentUpdate>(
        config.writerQueueSize,
        config.writerWaitTimeSec
    ) { dao.update(it) }

    suspend fun save(content: Content) {
        saveWorker.add(content)
    }

    suspend fun update(meta: ContentUpdate) {
        updateWorker.add(meta)
    }
}
