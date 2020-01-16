package com.roguepnz.memeagg.crawler

import com.roguepnz.memeagg.metrics.MetricsService
import com.roguepnz.memeagg.s3.S3Client
import com.roguepnz.memeagg.util.CoroutineWorkerPool
import io.micrometer.core.instrument.DistributionSummary
import io.micrometer.core.instrument.Timer

typealias UploadResult = String

class PayloadUploaderException(msg: String, cause: Throwable) : Exception(msg, cause)

class PayloadUploader(config: CrawlerConfig, private val s3: S3Client, private val metrics: MetricsService) {

    private val pool = CoroutineWorkerPool(config.maxConcurrentUploads)

    private val summary = DistributionSummary.builder("crawler.uploader.payloadsize")
        .description("payload size in kb")
//        .minimumExpectedValue(1)
//        .maximumExpectedValue()
        .register(metrics.registry)

    private val timer = Timer.builder("crawler.uploader.uploadspeed")
        .register(metrics.registry)

    suspend fun upload(key: String, payload: ByteArray, contentType: String): UploadResult {
        summary.record(payload.size / 1024.0)

        return pool.submit {
            try {
                s3.upload(key, payload, contentType)
            } catch (e: Exception) {
                throw PayloadUploaderException("UPLOAD FAILED: $key", e)
            }
        }
    }
}