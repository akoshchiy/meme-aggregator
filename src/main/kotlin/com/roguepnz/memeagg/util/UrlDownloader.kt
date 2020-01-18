package com.roguepnz.memeagg.util

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readBytes
import io.ktor.http.HttpStatusCode

class DownloadResult(val data: ByteArray, val contentType: String)

class UrlDownloaderException(msg: String, cause: Throwable?) : Exception(msg, cause) {
    constructor(msg: String) : this(msg, null)
}

class UrlDownloader(concurrentDownloads: Int, private val http: HttpClient) {

    private val pool = CoroutineWorkerPool(concurrentDownloads)

    suspend fun downloadString(url: String): String {
        return String(download(url).data)
    }

    suspend fun download(url: String): DownloadResult {
        return pool.submit {
            try {
                doDownload(url)
            } catch (e: Exception) {
                throw UrlDownloaderException("download failed url: $url", e)
            }
        }
    }

    private suspend fun doDownload(url: String): DownloadResult {
        val resp = http.get<HttpResponse>(url)
        if (resp.status != HttpStatusCode.OK) {
            throw UrlDownloaderException("status: ${resp.status.description}")
        }
        val contentType = resp.headers["content-type"]!!
        val bytes = resp.readBytes()
        return DownloadResult(bytes, contentType)
    }
}