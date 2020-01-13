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
               handleUpdate(key, item.raw)
            } else {
                scope.launch {
                    handleNew(key, item.raw)
                }
            }
        }
    }

    private suspend fun handleUpdate(key: String, raw: RawContent) {
        writer.updateMeta(
            Meta(
                key,
                raw.publishTime,
                raw.likesCount,
                raw.dislikesCount,
                raw.commentsCount
            )
        )
        logger.info("UP: ${JSON.stringify(raw)}")
    }

    private suspend fun handleNew(key: String, raw: RawContent) {
        val uploadRes = uploader.upload(key, raw.payload.url)

        writer.save(
            Content(
                null,
                raw.payload.type.code,
                uploadRes.url,
                uploadRes.hash,
                Meta(
                    key,
                    raw.publishTime,
                    raw.likesCount,
                    raw.dislikesCount,
                    raw.commentsCount
                )
            )
        )

        logger.info("CRAWL: ${JSON.stringify(raw)}")
    }
}