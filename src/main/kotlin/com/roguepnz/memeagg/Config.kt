package com.roguepnz.memeagg

import com.roguepnz.memeagg.cluster.NodeConfig
import com.roguepnz.memeagg.crawler.CrawlerConfig
import com.roguepnz.memeagg.s3.S3Config
import com.roguepnz.memeagg.http.ServerConfig
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.File

object Config {
    val db: Config = parse("./config/db.conf")
    val sources: Config = parse("./config/sources.conf")
    val crawler: CrawlerConfig = CrawlerConfig(parse("./config/crawler.conf"))
    val node: NodeConfig = NodeConfig(parse("./config/node.conf"))
    val s3: S3Config = S3Config(parse("./config/s3.conf"))
    val server: ServerConfig = ServerConfig(parse("./config/server.conf"))

    private fun parse(path: String): Config = ConfigFactory.parseFile(File(path)).resolve()
}