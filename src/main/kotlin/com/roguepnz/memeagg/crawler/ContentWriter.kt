package com.roguepnz.memeagg.crawler

import com.roguepnz.memeagg.core.dao.ContentDao
import com.roguepnz.memeagg.core.model.Content
import com.roguepnz.memeagg.core.model.Meta
import kotlinx.coroutines.CoroutineScope

class ContentWriter(config: CrawlerConfig, dao: ContentDao) {

    private val contentWorker = BatchWorker<Content>(config.writerQueueSize, config.writerWaitTimeSec) { dao.insert(it) }

    private val metaWorker = BatchWorker<Meta>(config.writerQueueSize, config.writerWaitTimeSec) { dao.updateMeta(it) }

//    fun start(scope: CoroutineScope) {
//        contentWorker.start(scope)
//        metaWorker.start(scope)
//    }

    suspend fun save(content: Content) {
        contentWorker.add(content)
    }

    suspend fun updateMeta(meta: Meta) {
        metaWorker.add(meta)
    }
}