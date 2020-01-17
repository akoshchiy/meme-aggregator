package com.roguepnz.memeagg.source.debeste

import com.typesafe.config.Config

class DebesteConfig(config: Config) {
    val maxPages = config.getInt("maxPages")
    val maxConcurrentDownloads = config.getInt("maxConcurrentDownloads")
    val updatePages = config.getInt("updatePages")
    val updateDelaySec = config.getInt("updateDelaySec")
}