package com.roguepnz.memeagg

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.File

object Config {
    val db: Config = parse("./config/db.conf")
    val sources: Config = parse("./config/sources.conf")
    val crawler: Config = parse("./config/crawler.conf")

    private fun parse(path: String): Config = ConfigFactory.parseFile(File(path)).resolve()
}