package com.roguepnz.memeagg.http

import com.typesafe.config.Config

class ServerConfig(val config: Config) {
    val port = config.getInt("port")
}