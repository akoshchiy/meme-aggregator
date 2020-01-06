package com.roguepnz.memeagg.cluster

import com.typesafe.config.Config

class NodeConfig(config: Config) {
    val workersCount = config.getInt("workersCount")
    val grabbedExpireTimeSec = config.getInt("grabbedExpireTimeSec")
    val checkGrabbedSec = config.getInt("checkGrabbedSec")
}