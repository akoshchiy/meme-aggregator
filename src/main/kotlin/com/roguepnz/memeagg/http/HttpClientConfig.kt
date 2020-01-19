package com.roguepnz.memeagg.http

import com.typesafe.config.Config

class HttpClientConfig(config: Config) {
    val socketTimeout = config.getInt("socketTimeout")
    val connectTimeout = config.getInt("connectTimeout")
    val connectionRequestTimeout = config.getInt("connectionRequestTimeout")
    val maxConnTotal = config.getInt("maxConnTotal")
    val maxConnPerRoute = config.getInt("maxConnPerRoute")
}