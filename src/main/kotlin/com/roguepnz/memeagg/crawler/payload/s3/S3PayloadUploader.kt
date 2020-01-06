package com.roguepnz.memeagg.crawler.payload.s3

import com.roguepnz.memeagg.crawler.payload.PayloadUploader
import software.amazon.awssdk.core.client.config.ClientAsyncConfiguration
import software.amazon.awssdk.services.s3.S3AsyncClient

class S3PayloadUploader : PayloadUploader {

    override suspend fun upload(): String {
//        S3AsyncClient.builder()
//            .endpointOverride()

        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}