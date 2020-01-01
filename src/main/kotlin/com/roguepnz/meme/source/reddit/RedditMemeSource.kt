package com.roguepnz.meme.source.reddit

import com.fasterxml.jackson.annotation.JsonProperty
import io.ktor.client.HttpClient
import io.ktor.client.request.get


data class RedditLink(
    val data: RedditLinkData
)

data class RedditLinkData(val thumbnail: String)

data class RedditListing(val data: RedditListingData)

data class RedditListingData(
    @JsonProperty("modhash") val modHash: String,
    val dist: Int,
    val children: List<RedditLink>
)

class RedditMemeSource(private val client: HttpClient) {

//    private val client: HttpClient = HttpClient(Apache) {
//        install(JsonFeature) {
//            serializer = JacksonSerializer {
//                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//                configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
//            }
//        }
//    }

    suspend fun load(): RedditListing {
        return client.get("https://reddit.com/r/ich_iel/new.json")
    }
}