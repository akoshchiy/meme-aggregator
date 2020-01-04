package com.roguepnz.memeagg.feed.api

import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.get

class HelloController : KtorController {

    override fun routing(): RoutingConf = {
        get("/hello") {
            call.respondText("sdad")
        }
    }
}