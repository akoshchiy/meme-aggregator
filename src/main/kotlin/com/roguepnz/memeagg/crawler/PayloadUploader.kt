package com.roguepnz.memeagg.crawler

import com.roguepnz.memeagg.s3.S3Client
import com.roguepnz.memeagg.util.CoroutineWorkerPool

typealias UploadResult = String

class PayloadUploaderException(msg: String, cause: Throwable) : Exception(msg, cause)

class PayloadUploader(config: CrawlerConfig, private val s3: S3Client) {

    private val pool = CoroutineWorkerPool(config.maxConcurrentUploads)

    suspend fun upload(key: String, payload: ByteArray, contentType: String): UploadResult {
        return pool.submit {
            try {
                s3.upload(key, payload, contentType)
            } catch (e: Exception) {
                throw PayloadUploaderException("UPLOAD FAILED: $key", e)
            }
        }
    }
}