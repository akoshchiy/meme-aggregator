package com.roguepnz.meme.api

import io.ktor.routing.Routing

typealias RoutingConf = Routing.() -> Unit

interface KtorController {
    fun routing(): RoutingConf
}