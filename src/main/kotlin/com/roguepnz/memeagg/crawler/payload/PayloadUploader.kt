package com.roguepnz.memeagg.crawler.payload

interface PayloadUploader {
    suspend fun upload(key: String, data: ByteArray, contentType: String): String
}