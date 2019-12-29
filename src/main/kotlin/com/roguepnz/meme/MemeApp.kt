package com.roguepnz.meme

import com.roguepnz.meme.api.FeedController
import com.roguepnz.meme.api.HelloController
import com.roguepnz.meme.source.RedditMemeSource
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking

fun main() {

    runBlocking {
        val listing = RedditMemeSource("ich_iel").load()
        println(listing)
    }


//    val server = embeddedServer(Netty, 8080) {
////        routing {
////            get("/") {
////                call.respondText("Hello, World!")
////            }
////        }
////        routing {
////
////        }
//
//        routing(FeedController().routing())
//        routing(HelloController().routing())
//    }
//
//    server.start(true)
}
