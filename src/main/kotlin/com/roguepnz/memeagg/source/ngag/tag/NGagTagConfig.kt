package com.roguepnz.memeagg.source.ngag.tag

import com.typesafe.config.Config

class NGagTagConfig(config: Config) {
    val tag: String = config.getString("tag")
    val bufferSize: Int = config.getInt("bufferSize")
}

