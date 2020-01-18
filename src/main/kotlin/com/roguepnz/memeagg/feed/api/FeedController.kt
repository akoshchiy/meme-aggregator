package com.roguepnz.memeagg.feed.api

import com.roguepnz.memeagg.core.dao.ContentDao
import com.roguepnz.memeagg.core.model.ContentView
import com.roguepnz.memeagg.core.model.Feed
import com.roguepnz.memeagg.http.KtorController
import com.roguepnz.memeagg.http.RoutingConf
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.util.getOrFail

class FeedController(private val contentDao: ContentDao) : KtorController {

    override fun routing(): RoutingConf = {
        get("/feed") {
            val p = call.parameters

            val count = if (p.contains("count")) p.getOrFail<Int>("count") else 10
            val after = if (p.contains("after")) p.getOrFail<String>("after") else null

            call.respond(getFeed(count, after))
        }

        get("/feed/{contentId}") {
            val id = call.parameters.getOrFail<String>("contentId")

            val content = getContent(id)

            if (content == null) {
                call.respond(HttpStatusCode.NotFound, "content not found")
            } else {
                call.respond(content)
            }
        }
    }

    private suspend fun getFeed(count: Int, after: String?): Feed {
        if (after == null) {
            return contentDao.getFeed(count)
        }
        val content = contentDao.getById(after)
        if (content == null) {
            return contentDao.getFeed(count)
        }
        return contentDao.getFeed(count, content.order)
    }


    private suspend fun getContent(id: String): ContentView? = contentDao.getViewById(id)
}