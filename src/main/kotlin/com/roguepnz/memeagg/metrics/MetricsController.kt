package com.roguepnz.memeagg.metrics

import com.roguepnz.memeagg.http.KtorController
import com.roguepnz.memeagg.http.RoutingConf
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.get

class MetricsController(private val metricsService: MetricsService) : KtorController {

    override fun routing(): RoutingConf = {
        get("/metrics/prometheus") {
            val resp = metricsService.scrapePrometheus()
            call.respond(resp)
        }
    }
}