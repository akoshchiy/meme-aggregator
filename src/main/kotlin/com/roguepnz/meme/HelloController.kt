package com.roguepnz.meme

import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.get

class HelloController : KtorController {

    override fun routing(): Routes = {
        get("/hello") {
            call.respondText("sdad")
        }
    }
}