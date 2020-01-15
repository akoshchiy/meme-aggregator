package com.roguepnz.memeagg.source.ngag.group

import com.typesafe.config.Config

class NGagGroupConfig(config: Config) {
    val group = config.getString("group")
    val lastUpdateCount = config.getInt("lastUpdateCount")

}