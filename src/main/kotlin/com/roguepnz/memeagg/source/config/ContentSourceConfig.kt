package com.roguepnz.memeagg.source.config

import com.typesafe.config.Config

class ContentSourceConfig(config: Config) {
    val id: String  = config.getString("id")
    val type: SourceType = SourceType.valueOf(config.getString("type").toUpperCase())
    val config: Config = config.getConfig("config")
}