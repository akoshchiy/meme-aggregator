package com.roguepnz.meme

import com.roguepnz.meme.api.KtorController
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {

//    runBlocking {
//        val listing = RedditMemeSource("ich_iel").load()
//        println(listing)
//    }


    val server = embeddedServer(Netty, 8080) {
        //        routing {
//            get("/") {
//                call.respondText("Hello, World!")
//            }
//        }
//        routing {
//
//        }
        AppContainer.getAll(KtorController::class)
            .forEach {
                routing(it.routing())
            }


//        routing(FeedController().routing())
//        routing(HelloController().routing())
    }

    server.start(true)
}
