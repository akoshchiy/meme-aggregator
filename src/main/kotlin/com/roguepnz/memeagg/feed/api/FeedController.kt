package com.roguepnz.memeagg.feed.api

import com.roguepnz.memeagg.core.dao.ContentDao
import com.roguepnz.memeagg.core.model.Content
import com.roguepnz.memeagg.http.KtorController
import com.roguepnz.memeagg.http.RoutingConf
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.get

class FeedController(private val contentDao: ContentDao) : KtorController {

    override fun routing(): RoutingConf = {
        get("/feed") {
            call.respond(getFeed())
        }

        get("/feed/{contentId}") {
            // TODO handle errors
            val content = getContent(call.parameters["contentId"]!!)!!
            call.respond(content)
        }
    }

    private suspend fun getFeed(): List<Content> = contentDao.getPage()
    private suspend fun getContent(id: String): Content? = contentDao.getById(id)
}