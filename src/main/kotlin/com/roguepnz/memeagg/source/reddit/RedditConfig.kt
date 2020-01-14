package com.roguepnz.memeagg.source.reddit

import com.typesafe.config.Config

class RedditConfig(config: Config) {
    val clientId: String = config.getString("clientId")
    val secret: String = config.getString("secret")
    val userAgent: String = config.getString("userAgent")
}