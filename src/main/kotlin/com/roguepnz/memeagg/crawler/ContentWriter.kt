package com.roguepnz.memeagg.crawler

import com.roguepnz.memeagg.core.dao.ContentDao
import com.roguepnz.memeagg.core.model.Content
import com.roguepnz.memeagg.core.model.ContentUpdate
import com.roguepnz.memeagg.metrics.MetricsService
import com.roguepnz.memeagg.util.BatchWorker
import com.roguepnz.memeagg.util.Times
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.DistributionSummary
import io.micrometer.core.instrument.Gauge

class ContentWriter(config: CrawlerConfig, dao: ContentDao, metrics: MetricsService) {

    private val startTime = Times.now()

    private val requestsCount = Counter.builder("crawler.writer.requestscount")
        .description("total db requests count")
        .register(metrics.registry)

    private val itemsCount = DistributionSummary.builder("crawler.writer.itemscount")
        .description("total writed items count")
        .register(metrics.registry)

    init {
        Gauge.builder("crawler.writer.requestsrate", this::getRequestsRate)
            .description("requests rate in req/s")
            .register(metrics.registry)

        Gauge.builder("crawler.writer.itemsrate", this::getItemsRate)
            .description("items rate in item/s")
            .register(metrics.registry)
    }

    private fun getItemsRate(): Double {
        val secs = Times.now() - startTime
        return itemsCount.totalAmount() / secs
    }

    private fun getRequestsRate(): Double {
        val secs = Times.now() - startTime
        return requestsCount.count() / secs
    }

    private val saveWorker = BatchWorker<Content>(
        config.writerQueueSize,
        config.writerWaitTimeSec
    ) {
        requestsCount.increment()
        itemsCount.record(it.size.toDouble())
        dao.insert(it)
    }

    private val updateWorker = BatchWorker<ContentUpdate>(
        config.writerQueueSize,
        config.writerWaitTimeSec
    ) {
        requestsCount.increment()
        itemsCount.record(it.size.toDouble())
        dao.update(it)
    }

    suspend fun save(content: Content) {
        saveWorker.add(content)
    }

    suspend fun update(meta: ContentUpdate) {
        updateWorker.add(meta)
    }
}
