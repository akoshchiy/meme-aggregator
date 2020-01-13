package com.roguepnz.memeagg.crawler

import com.roguepnz.memeagg.core.dao.ContentDao
import com.roguepnz.memeagg.core.model.Content
import com.roguepnz.memeagg.core.model.Meta
import com.roguepnz.memeagg.crawler.payload.PayloadUploader
import com.roguepnz.memeagg.source.ContentSource
import com.roguepnz.memeagg.source.model.RawContent
import com.roguepnz.memeagg.util.JSON
import com.roguepnz.memeagg.util.loggerFor
import kotlinx.coroutines.*

class ContentCrawler(private val writer: ContentWriter,
                     private val contentDao: ContentDao,
                     private val uploader: PayloadUploader) {

    private val logger = loggerFor<ContentCrawler>()

    private class BatchItem(val sourceId: String, val raw: RawContent)

    private val batchWorker = BatchWorker(1000, 1, this::handleBatch)

    fun start(scope: CoroutineScope) {
        batchWorker.start(scope)
    }

    fun crawl(scope: CoroutineScope, sourceId: String, source: ContentSource) {
        scope.launch {
            val channel = source.listen(this)
            for (raw in channel) {
                batchWorker.add(BatchItem(sourceId, raw))
            }
        }
    }

    private suspend fun handleBatch(scope: CoroutineScope, batch: List<BatchItem>) {
        val keys = batch.map {"${it.sourceId}_${it.raw.id}"}

        val set = contentDao.contains(keys)

        for (item in batch) {
            val key = "${item.sourceId}_${item.raw.id}"
            if (set.contains(key)) {
               handleUpdate(item.raw)
            } else {
                scope.launch {
                    handleNew(key, item.raw)
                }
            }
        }
    }

    private suspend fun handleUpdate(raw: RawContent) {
        writer.update(
            Meta(
                raw.publishTime,
                raw.likesCount,
                raw.dislikesCount,
                raw.commentsCount,
                0
            )
        )
        logger.info("UP: ${JSON.stringify(raw)}")
    }

    private suspend fun handleNew(key: String, raw: RawContent) {
        val uploadRes = uploader.upload(key, raw.payload.url)

        writer.insert(
            Content(
                null,
                key,
                raw.payload.type.code,
                uploadRes.url,
                uploadRes.hash,
                raw.publishTime,
                raw.likesCount,
                raw.dislikesCount,
                raw.commentsCount,
                0
            )
        )

        logger.info("CRAWL: ${JSON.stringify(raw)}")
    }
}