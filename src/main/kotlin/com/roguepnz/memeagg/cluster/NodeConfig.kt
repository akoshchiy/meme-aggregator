package com.roguepnz.memeagg.cluster

import com.typesafe.config.Config

class NodeConfig(config: Config) {
    val workersCount = config.getInt("workersCount")
    val grabbedExpireTimeSec = config.getLong("grabbedExpireTimeSec")
    val checkGrabbedSec = config.getLong("checkGrabbedSec")
    val grabDelaySec = config.getLong("grabDelaySec")
}