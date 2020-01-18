package com.roguepnz.memeagg.http

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature

object HttpClientBuilder {

    fun build(): HttpClient {
        return HttpClient(Apache) {
            engine {
                connectionRequestTimeout
                customizeClient {

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