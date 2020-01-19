package com.roguepnz.memeagg.http

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature

object HttpClientBuilder {

    fun build(config: HttpClientConfig): HttpClient {
        return HttpClient(Apache) {
            engine {
                connectionRequestTimeout = config.connectionRequestTimeout
                connectTimeout = config.connectTimeout
                socketTimeout = config.socketTimeout
                customizeClient {
                    setMaxConnTotal(config.maxConnTotal)
                    setMaxConnPerRoute(config.maxConnPerRoute)
                }
            }
            install(JsonFeature) {
                serializer = JacksonSerializer {
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
                }
            }
        }
    }
}