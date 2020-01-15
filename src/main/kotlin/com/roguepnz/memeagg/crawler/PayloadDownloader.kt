package com.roguepnz.memeagg.crawler

import com.roguepnz.memeagg.util.CoroutineWorkerPool
import com.roguepnz.memeagg.util.Hashes
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readBytes

class DownloadResult(val data: ByteArray, val contentType: String, val hash: String)

class PayloadDownloaderException(msg: String): Exception(msg)

class PayloadDownloader(config: CrawlerConfig, private val http: HttpClient) {

    private val pool = CoroutineWorkerPool(config.maxConcurrentDownloads)

    suspend fun download(url: String): DownloadResult {
        return pool.submit {
            try {
                doDownload(url)
            } catch (e: Exception) {
                throw PayloadDownloaderException("download failed url: $url")
            }
        }
    }

    private suspend fun doDownload(url: String): DownloadResult {
        val resp = http.get<HttpResponse>(url)
        val contentType = resp.headers["content-type"]!!
        val bytes = resp.readBytes()
        val hash = Hashes.md5(bytes)
        return DownloadResult(bytes, contentType, hash)
    }
}