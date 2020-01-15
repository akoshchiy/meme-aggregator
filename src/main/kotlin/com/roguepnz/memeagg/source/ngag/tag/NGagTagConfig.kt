package com.roguepnz.memeagg.source.ngag.tag

import com.typesafe.config.Config

class NGagTagConfig(config: Config) {
    val tag = config.getString("tag")
    val lastUpdateCount = config.getInt("lastUpdateCount")
}

