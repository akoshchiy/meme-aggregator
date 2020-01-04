package com.roguepnz.memeagg.source.ngag.group

import com.typesafe.config.Config

class NGagGroupConfig(config: Config) {
    val group: String = config.getString("group")
    val bufferSize: Int = config.getInt("bufferSize")
}