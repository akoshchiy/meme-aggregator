package com.roguepnz.memeagg.crawler

import com.roguepnz.memeagg.core.dao.ContentDao
import com.roguepnz.memeagg.core.model.Content
import com.roguepnz.memeagg.core.model.Meta
import com.roguepnz.memeagg.source.ContentSource
import com.roguepnz.memeagg.source.model.RawContent
import com.roguepnz.memeagg.util.*
import kotlinx.coroutines.*

class ContentCrawler(config: CrawlerConfig,
                     private val writer: ContentWriter,
                     private val contentDao: ContentDao,
                     private val uploader: PayloadUploader,
                     private val downloader: UrlDownloader) {

    private val logger = loggerFor<ContentCrawler>()

    private class BatchItem(val sourceId: String, val raw: RawContent)

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val batchWorker = BatchWorker(config.crawlerQueueSize, config.crawlerWaitTimeSec, this::handleBatch)

    fun crawl(sourceId: String, source: ContentSource) {
        scope.launch {
            val channel = source.listen()
            for (raw in channel) {
                batchWorker.add(BatchItem(sourceId, raw))
                logger.info("CRAWL: ${JSON.stringify(raw)}")
            }
        }
    }

    private suspend fun handleBatch(batch: List<BatchItem>) {
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
        val downloadRes = downloader.download(raw.payload.url)
        val url = uploader.upload(key, downloadRes.data, downloadRes.contentType)

        val hash = Hashes.md5(downloadRes.data)

        writer.save(
            Content(
                null,
                raw.payload.type.code,
                url,
                hash,
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