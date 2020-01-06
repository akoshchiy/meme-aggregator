package com.roguepnz.memeagg.crawler.payload

interface PayloadUploader {
    suspend fun upload(): String
}