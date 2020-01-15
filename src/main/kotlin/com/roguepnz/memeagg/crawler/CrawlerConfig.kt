package com.roguepnz.memeagg.crawler

import com.typesafe.config.Config

class CrawlerConfig(config: Config) {
    val writerQueueSize = config.getInt("writerQueueSize")
    val writerWaitTimeSec = config.getInt("writerWaitTimeSec")

    val crawlerQueueSize = config.getInt("crawlerQueueSize")
    val crawlerWaitTimeSec = config.getInt("crawlerWaitTimeSec")

    val maxConcurrentUploads = config.getInt("maxConcurrentUploads")
    val maxConcurrentDownloads = config.getInt("maxConcurrentDownloads")
}