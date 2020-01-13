package com.roguepnz.memeagg

import ch.qos.logback.classic.util.ContextInitializer
import com.roguepnz.memeagg.cluster.NodeService
import com.roguepnz.memeagg.http.KtorController
import com.roguepnz.memeagg.crawler.ContentCrawler
import com.roguepnz.memeagg.crawler.ContentWriter
import com.roguepnz.memeagg.crawler.payload.PayloadUploader
import com.roguepnz.memeagg.db.Dao
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

fun main() {
    runBlocking {
        System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "./config/logback.xml")

        val jobs = AppContainer.getAll(Dao::class).map {
            GlobalScope.async {
                it.init()
            }
        }

        jobs.forEach { it.join() }

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

        val scope = CoroutineScope(Dispatchers.IO)

        val writer = AppContainer.get(ContentWriter::class)
        writer.start(scope)

        val uploader = AppContainer.get(PayloadUploader::class)
        uploader.start(scope)

        val crawler = AppContainer.get(ContentCrawler::class)
        crawler.start(scope)

        val nodeService = AppContainer.get(NodeService::class)
        nodeService.start(scope)

        server.start(true)
    }
}
