package com.roguepnz.memeagg.source.reddit

import com.fasterxml.jackson.annotation.JsonProperty
import com.roguepnz.memeagg.crawler.CoroutineWorkerPool
import com.roguepnz.memeagg.util.Times
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.http.userAgent
import java.util.*
import kotlin.math.abs

private const val AUTH_URL = "https://www.reddit.com/api/v1/access_token"
private const val EXPIRE_DIFF = 60

class RedditClient(private val config: RedditConfig, private val client: HttpClient) {

    private val pool: CoroutineWorkerPool = CoroutineWorkerPool(1)

    private var token: Token? = null

    suspend fun getPosts(subreddits: List<String>, after: String? = null): RedditListing {
        return pool.submit {
            checkToken()
            val cursor = if (after != null) "after=${after}" else ""
            val url = "https://www.reddit.com/r/${subreddits.joinToString("+")}/new.json?$cursor"
            client.get<RedditListing>(url)
        }
    }

     suspend fun auth(): Token {
        val auth = Base64.getEncoder().encodeToString("${config.clientId}:${config.secret}".toByteArray())

        val resp = client.post<AuthResponse>(AUTH_URL) {
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
        val resp = client.post<AuthResponse>(AUTH_URL) {
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

        val expired: Boolean get() {
            return abs(Times.now() - expireTime) < EXPIRE_DIFF
        }
    }
}

data class AuthResponse(val message: String?,
                        val error: Boolean?,
                        @JsonProperty("expires_in") val expiresIn: Int?,
                        @JsonProperty("access_token") val accessToken: String?,
                        @JsonProperty("refresh_token") val refreshToken: String?)

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
