package com.roguepnz.memeagg.feed.api

import io.ktor.routing.Routing

typealias RoutingConf = Routing.() -> Unit

interface KtorController {
    fun routing(): RoutingConf
}