package com.roguepnz.memeagg.s3

import kotlinx.coroutines.future.await
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.async.AsyncRequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.*
import java.net.URI

class S3Client(private val config: S3Config) {

    private val client = buildClient()

    private fun buildClient(): S3AsyncClient {
        val credentials = AwsBasicCredentials.create(config.accessKey, config.secretKey)

        val s3Config = S3Configuration.builder()
            .pathStyleAccessEnabled(true)
            .checksumValidationEnabled(false)
            .build()

        return S3AsyncClient.builder()
            .region(Region.AP_NORTHEAST_1)
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .serviceConfiguration(s3Config)
            .endpointOverride(URI.create(config.endpoint))
            .build()
    }

    private suspend fun createBucket(bucket: String) {
        val req = CreateBucketRequest.builder()
            .bucket(bucket)
            .acl(BucketCannedACL.PUBLIC_READ)
            .build()

        client.createBucket(req).await()
    }

    suspend fun upload(key: String, data: ByteArray, contentType: String): String {
//        createBucket(config.bucket)

        val req = PutObjectRequest.builder()
            .bucket(config.bucket)
            .acl(ObjectCannedACL.PUBLIC_READ)
            .contentType(contentType)
            .key(key)
            .build()

        client.putObject(req, AsyncRequestBody.fromBytes(data)).await()

        val urlReq = GetUrlRequest.builder()
            .bucket(config.bucket)
            .key(key)
            .endpoint(URI.create(config.endpoint))
            .region(Region.AP_NORTHEAST_1)
            .build()

        return client.utilities().getUrl(urlReq).toString()
    }
}