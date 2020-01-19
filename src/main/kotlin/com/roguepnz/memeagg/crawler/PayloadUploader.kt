package com.roguepnz.memeagg.crawler

import com.roguepnz.memeagg.metrics.MetricsService
import com.roguepnz.memeagg.s3.S3Client
import com.roguepnz.memeagg.util.CoroutineWorkerPool
import com.roguepnz.memeagg.util.Times
import io.micrometer.core.instrument.DistributionSummary
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.Timer
import java.sql.Time
import java.util.concurrent.atomic.AtomicInteger

typealias UploadUrl = String

class PayloadUploaderException(msg: String, cause: Throwable) : Exception(msg, cause)

class PayloadUploader(config: CrawlerConfig, private val s3: S3Client, private val metrics: MetricsService) {

    private val pool = CoroutineWorkerPool(config.maxConcurrentUploads)

    private val startTime = Times.now()

    private val payloadSize: DistributionSummary = DistributionSummary.builder("crawler.uploader.payloadsize")
        .description("payload size in kb")
//        .minimumExpectedValue(1)
//        .maximumExpectedValue()
        .register(metrics.registry)

    init {
        Gauge.builder("crawler.uploader.uploadspeed", this::getUploadSpeed)
            .description("upload speed in kb/s")
            .register(metrics.registry)
    }

    private fun getUploadSpeed(): Double {
        val secs = Times.now() - startTime
        return (payloadSize.totalAmount()) / secs
    }

    suspend fun upload(key: String, payload: ByteArray, contentType: String): UploadUrl {
        return pool.submit {
            try {
                val url = s3.upload(key, payload, contentType)
                payloadSize.record(payload.size / 1024.0)
                url
            } catch (e: Exception) {
                throw PayloadUploaderException("upload failed: $key", e)
            }
        }
    }
}