package com.roguepnz.meme

import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.get

class FeedController : KtorController {


    override fun routing(): Routes = {
        get("/") {
            call.respondText("Hello, World!")
        }
    }
}