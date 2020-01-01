package com.roguepnz.meme.source.ngag

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.roguepnz.meme.source.ngag.api.NGagClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import kotlinx.coroutines.runBlocking
import org.junit.Test

class NGagClientTest {

    @Test
    fun testLoad() {
        val httpClient = HttpClient(Apache) {
            install(JsonFeature) {
                serializer = JacksonSerializer {
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
                }
            }
        }

        val client = NGagClient(httpClient)

        runBlocking {
            for (i in 0 .. 500 step 10) {
                loadPosts(client, i)

//                async {
//                }
            }
        }
    }

    suspend fun loadPosts(client: NGagClient, offset: Int) {
        val result = client.getPosts("germany", offset)
        val size = result.data.posts.size
        println("Done. offset: $offset, size: $size")
    }
}