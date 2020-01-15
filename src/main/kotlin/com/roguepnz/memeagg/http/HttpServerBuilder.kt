package com.roguepnz.memeagg.http

import com.roguepnz.memeagg.AppContainer
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

object HttpServerBuilder {

    fun build(config: ServerConfig, controllers: List<KtorController>): ApplicationEngine {
        return embeddedServer(Netty, config.port) {
            install(ContentNegotiation) {
                jackson {
                }
            }
            controllers.forEach {
                routing(it.routing())
            }
        }
    }
}