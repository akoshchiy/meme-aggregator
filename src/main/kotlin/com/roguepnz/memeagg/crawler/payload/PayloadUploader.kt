package com.roguepnz.memeagg.crawler.payload

import com.roguepnz.memeagg.crawler.CoroutineWorkerPool
import com.roguepnz.memeagg.crawler.payload.s3.S3Client
import com.roguepnz.memeagg.util.Hashes
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readBytes
import kotlinx.coroutines.CoroutineScope

class UploadResult(val key: String, val url: String, val hash: String)

class PayloadUploader(private val s3: S3Client, private val http: HttpClient) {

    private val pool = CoroutineWorkerPool(100)

    fun start(scope: CoroutineScope) {
        pool.start(scope)
    }

    suspend fun upload(key: String, downloadUrl: String): UploadResult {
        return pool.submitAsync { doUpload(key, downloadUrl) }.await()
    }

    private suspend fun doUpload(key: String, downloadUrl: String): UploadResult {
        val resp = http.get<HttpResponse>(downloadUrl)
        val contentType = resp.headers["content-type"]
        val bytes = resp.readBytes()
        val hash = Hashes.md5(bytes)
        val url = s3.upload(key, bytes, contentType!!)
        return UploadResult(key, url, hash)
    }
}