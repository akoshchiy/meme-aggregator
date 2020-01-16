package com.roguepnz.memeagg.metrics

import io.micrometer.core.instrument.composite.CompositeMeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry

class MetricsService {
    private val prometheus = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    private val simple = SimpleMeterRegistry()

    val registry = CompositeMeterRegistry()
        .add(prometheus)
        .add(simple)

    fun scrapePrometheus(): String {
        return prometheus.scrape()
    }


}