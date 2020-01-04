package com.roguepnz.memeagg

import com.roguepnz.memeagg.feed.api.KtorController
import com.roguepnz.memeagg.crawler.ContentCrawler
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    val server = embeddedServer(Netty, 8080) {
        AppContainer.getAll(KtorController::class)
            .forEach {
                routing(it.routing())
            }
    }

    val crawler = AppContainer.get(ContentCrawler::class)
    crawler.start()

    server.start(true)
}
