package com.roguepnz.memeagg

import com.roguepnz.memeagg.cluster.NodeConfig
import com.roguepnz.memeagg.crawler.CrawlerConfig
import com.roguepnz.memeagg.http.HttpClientConfig
import com.roguepnz.memeagg.s3.S3Config
import com.roguepnz.memeagg.http.ServerConfig
import com.roguepnz.memeagg.util.JSON
import com.roguepnz.memeagg.util.loggerFor
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.File

object Configs {
    private val logger = loggerFor<Configs>()

    val db: Config = parse("./config/db.conf")
    val sources: Config = parse("./config/sources.conf")
    val crawler: CrawlerConfig = CrawlerConfig(parse("./config/crawler.conf"))
    val node: NodeConfig = NodeConfig(parse("./config/node.conf"))
    val s3: S3Config = S3Config(parse("./config/s3.conf"))
    val server: ServerConfig = ServerConfig(parse("./config/server.conf"))
    val http: HttpClientConfig = HttpClientConfig(parse("./config/http.conf"))

    private fun parse(path: String): Config = ConfigFactory.parseFile(File(path)).resolve()

    init {
        logger.info("====== CONFIGS ======")
        logger.info("db: $db")
        logger.info("sources: $sources")
        logger.info("crawler: ${JSON.stringify(crawler)}")
        logger.info("node: ${JSON.stringify(node)}")
        logger.info("s3: ${JSON.stringify(s3)}")
        logger.info("server: ${JSON.stringify(server)}")
        logger.info("http: ${JSON.stringify(http)}")
        logger.info("=====================")
    }
}