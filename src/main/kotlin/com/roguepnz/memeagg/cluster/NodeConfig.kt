package com.roguepnz.memeagg.cluster

import com.typesafe.config.Config

class NodeConfig(config: Config) {
    val maxSourcesCount = config.getInt("maxSourcesCount")
    val grabbedExpireTimeSec = config.getLong("grabbedExpireTimeSec")
    val checkGrabbedSec = config.getLong("checkGrabbedSec")
}