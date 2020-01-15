package com.roguepnz.memeagg.s3

import com.typesafe.config.Config

class S3Config(config: Config) {
    val accessKey: String = config.getString("accessKey")
    val secretKey: String = config.getString("secretKey")
    val endpoint: String = config.getString("endpoint")
    val bucket: String = config.getString("bucket")
}