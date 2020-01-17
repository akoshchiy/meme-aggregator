package com.roguepnz.memeagg.source.reddit

import com.fasterxml.jackson.annotation.JsonProperty
import com.roguepnz.memeagg.core.model.ContentType
import com.roguepnz.memeagg.util.CoroutineWorkerPool
import com.roguepnz.memeagg.source.model.Payload
import com.roguepnz.memeagg.source.model.RawContent
import com.roguepnz.memeagg.util.JSON
import com.roguepnz.memeagg.util.Times
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.http.contentLength
import io.ktor.http.userAgent
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.math.abs

private const val AUTH_URL = "https://www.reddit.com/api/v1/access_token"
private const val EXPIRE_DIFF = 60

class RedditClient(private val config: RedditConfig, private val http: HttpClient) {

    private val pool: CoroutineWorkerPool = CoroutineWorkerPool(1)

    private var token: Token? = null

    suspend fun getContent(subreddits: List<String>, after: String?): RedditContentResult {
        return pool.submit {
            checkToken()
            val cursor = if (after != null) "after=${after}" else ""
            val url = "https://oauth.reddit.com/r/${subreddits.joinToString("+")}/new.json?$cursor"
            extractContent(http.get(url) {
                header("Authorization", "Bearer ${token?.accessToken}")
                userAgent(config.userAgent)
            })
        }
    }

    private fun extractContent(listing: RedditListing): RedditContentResult {
        val content = listing.data.children
            .asSequence()
            .filter { it.data.isVideo || it.data.isImage }
            .map {
                RawContent(
                    it.data.name,
                    it.data.title,
                    it.data.authorFullName,
                    extractPayload(it.data),
                    it.data.createdUtc,
                    0,
                    0,
                    it.data.ups,
                    it.data.numComments
                )
            }
            .toList()

        return RedditContentResult(content, listing.data.after)
    }

    private fun extractPayload(data: RedditLinkData): Payload {
        return when {
            data.isVideo -> Payload(ContentType.VIDEO, data.media!!.redditVideo!!.fallbackUrl)
            data.isImage -> Payload(ContentType.IMAGE, data.url)
            else -> throw IllegalArgumentException("unsupported type")
        }
    }

    suspend fun auth(): Token {
        val auth = Base64.getEncoder().encodeToString("${config.clientId}:${config.secret}".toByteArray())

        val resp = http.post<AuthResponse>(AUTH_URL) {
            parameter("grant_type", "client_credentials")
            parameter("scope", "read")
            parameter("duration", "permanent")
            header("Authorization", "Basic $auth")
            userAgent(config.userAgent)
        }

        if (resp.error != null) {
            throw RedditException(resp.message!!)
        }

        return Token(
            resp.accessToken!!,
            Times.now() + resp.expiresIn!!,
            resp.refreshToken!!
        )
    }

    private suspend fun refresh(): Token {
        val resp = http.post<AuthResponse>(AUTH_URL) {
            parameter("grant_type", "refresh_token")
            parameter("refresh_toke", token!!.refreshToken)
            userAgent(config.userAgent)
        }
        if (resp.error != null) {
            throw RedditException(resp.message!!)
        }
        return Token(
            resp.accessToken!!,
            Times.now() + resp.expiresIn!!,
            resp.refreshToken!!
        )
    }

    private suspend fun checkToken() {
        if (token == null) {
            token = auth()
            return
        }
        if (token!!.expired) {
            token = refresh()
        }
    }

    class Token(val accessToken: String, val expireTime: Int, val refreshToken: String) {

        val expired: Boolean
            get() {
                return abs(Times.now() - expireTime) < EXPIRE_DIFF
            }
    }
}

data class RedditContentResult(val data: List<RawContent>, val after: String?)

data class AuthResponse(val message: String?,
                        val error: Boolean?,
                        @JsonProperty("expires_in") val expiresIn: Int?,
                        @JsonProperty("access_token") val accessToken: String?,
                        @JsonProperty("refresh_token") val refreshToken: String?)

data class RedditLink(
    val data: RedditLinkData
)

data class RedditLinkData(@JsonProperty("num_comments") val numComments: Int,
                          val ups: Int,
                          @JsonProperty("is_video") val isVideo: Boolean,
                          @JsonProperty("created_utc") val createdUtc: Int,
                          val media: RedditMedia?,
                          val preview: Any?,
                          val url: String,
                          val name: String,
                          val title: String,
                          @JsonProperty("author_fullname") val authorFullName: String) {
    val isImage get() = preview != null
}

data class RedditMedia(@JsonProperty("reddit_video") val redditVideo: ReddiVideo?)

data class ReddiVideo(@JsonProperty("fallback_url") val fallbackUrl: String)

data class RedditListing(val data: RedditListingData)

data class RedditListingData(@JsonProperty("modhash") val modHash: String,
                             val dist: Int,
                             val children: List<RedditLink>,
                             val after: String?)
