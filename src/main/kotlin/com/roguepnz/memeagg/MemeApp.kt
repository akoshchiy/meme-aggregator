package com.roguepnz.memeagg

import ch.qos.logback.classic.util.ContextInitializer
import com.roguepnz.memeagg.cluster.NodeService
import com.roguepnz.memeagg.http.KtorController
import com.roguepnz.memeagg.db.Dao
import com.roguepnz.memeagg.db.DaoInitializer
import com.roguepnz.memeagg.http.HttpServerBuilder
import kotlinx.coroutines.*

fun main() {
    runBlocking {
        System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "./config/logback.xml")

        DaoInitializer.init(AppContainer.getAll(Dao::class))

        val nodeService = AppContainer.get(NodeService::class)
        nodeService.start()

        val server = HttpServerBuilder.build(Config.server, AppContainer.getAll(KtorController::class))
        server.start(true)
    }
}
