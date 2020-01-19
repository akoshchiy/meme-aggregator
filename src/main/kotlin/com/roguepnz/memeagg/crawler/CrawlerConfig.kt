package com.roguepnz.memeagg.crawler

import com.typesafe.config.Config

class CrawlerConfig(config: Config) {
    val writerBatchSize = config.getInt("writerBatchSize")
    val writerWaitTimeSec = config.getInt("writerWaitTimeSec")

    val crawlerBatchSize = config.getInt("crawlerBatchSize")
    val crawlerWaitTimeSec = config.getInt("crawlerWaitTimeSec")

    val maxConcurrentUploads = config.getInt("maxConcurrentUploads")
    val maxConcurrentDownloads = config.getInt("maxConcurrentDownloads")
}