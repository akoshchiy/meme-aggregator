package com.roguepnz.memeagg.api

import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.get

class FeedController : KtorController {

    override fun routing(): RoutingConf = {
        get("/") {
            call.respondText("Hello, World!")
        }
    }
}