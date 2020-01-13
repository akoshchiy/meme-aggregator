package com.roguepnz.memeagg.crawler

import com.roguepnz.memeagg.core.dao.ContentDao
import com.roguepnz.memeagg.core.model.Content
import com.roguepnz.memeagg.core.model.Meta
import kotlinx.coroutines.CoroutineScope

class ContentWriter(config: CrawlerConfig, dao: ContentDao) {

    private val worker = BatchWorker(config.writerQueueSize, config.writerWaitTimeSec, prepareWork(dao))


    private fun prepareWork(dao: ContentDao): suspend (CoroutineScope, List<Content>) -> Unit {
        return  { _, c ->
            dao.save(c)
        }
    }

    fun start(scope: CoroutineScope) {
        worker.start(scope)
    }

    suspend fun insert(content: Content) {
        worker.add(content)
    }

    suspend fun update(meta: Meta) {
    }
}