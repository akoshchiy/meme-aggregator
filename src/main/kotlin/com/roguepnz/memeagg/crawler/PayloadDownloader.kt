package com.roguepnz.memeagg.crawler

import com.roguepnz.memeagg.metrics.MetricsService
import com.roguepnz.memeagg.util.DownloadResult
import com.roguepnz.memeagg.util.Times
import com.roguepnz.memeagg.util.UrlDownloader
import io.micrometer.core.instrument.DistributionSummary
import io.micrometer.core.instrument.Gauge

class PayloadDownloader(private val downloader: UrlDownloader, metrics: MetricsService) {

    private val startTime = Times.now()

    private val payloadSize: DistributionSummary = DistributionSummary.builder("crawler.downloader.payloadsize")
        .description("payload size in kb")
//        .minimumExpectedValue(1)
//        .maximumExpectedValue()
        .register(metrics.registry)

    init {
        Gauge.builder("crawler.downloader.downloadspeed", this::getDownloadSpeed)
            .description("download speed in kb/s")
            .register(metrics.registry)
    }

    private fun getDownloadSpeed(): Double {
        val secs = Times.now() - startTime
        return (payloadSize.totalAmount()) / secs
    }

    suspend fun download(url: String): DownloadResult {
        val res = downloader.download(url)
        payloadSize.record(res.data.size / 1024.0)
        return res
    }
}