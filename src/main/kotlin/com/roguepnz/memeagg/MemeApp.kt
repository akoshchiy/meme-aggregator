package com.roguepnz.memeagg

import com.roguepnz.memeagg.http.KtorController
import com.roguepnz.memeagg.crawler.ContentCrawler
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    val server = embeddedServer(Netty, 8080) {
        install(ContentNegotiation) {
            jackson {

            }


        }
        AppContainer.getAll(KtorController::class)
            .forEach {
                routing(it.routing())
            }
    }

    val crawler = AppContainer.get(ContentCrawler::class)
    crawler.start()

    server.start(true)
}
