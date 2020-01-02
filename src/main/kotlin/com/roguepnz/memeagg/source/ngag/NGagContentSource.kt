package com.roguepnz.memeagg.source.ngag

import com.roguepnz.memeagg.model.ContentType
import com.roguepnz.memeagg.source.ContentSource
import com.roguepnz.memeagg.source.model.Payload
import com.roguepnz.memeagg.source.model.RawContent
import com.roguepnz.memeagg.source.ngag.api.NGagClient
import com.roguepnz.memeagg.source.ngag.api.NGagPost
import com.roguepnz.memeagg.source.ngag.api.NGagPostResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch

class NGagContentSource(private val config: NGagSourceConfig,
                        private val client: NGagClient) : ContentSource {

    override fun listen(): ReceiveChannel<RawContent> {
        val channel = Channel<RawContent>(100)

        GlobalScope.launch(Dispatchers.IO) {
            var offset = 0

            while (true) {
                val result = client.getPosts(config.tag, offset)
                val content = extractContent(result)
                if (content.isEmpty()) {
                    // TODO crawl is over
                    break
                }
                content.forEach {
                    channel.offer(it)
                }
                offset = result.data.nextOffset ?: break
            }
        }

        return channel
    }

    private fun extractContent(result: NGagPostResult): List<RawContent> {
        return result.data.posts
            .map {
                RawContent(
                    it.id,
                    it.title,
                    extractPayload(it),
                    it.creationTs,
                    it.upVoteCount,
                    it.downVoteCount,
                    it.commentsCount
                )
            }
    }

    private fun extractPayload(post: NGagPost): Payload =
        when {
            post.isPhoto -> Payload(ContentType.IMAGE, post.extractUrl(".jpg"))
            post.isVideo -> Payload(ContentType.VIDEO, post.extractUrl(".webp"))
            post.isAnimated -> Payload(ContentType.VIDEO, post.extractUrl(".mp4"))
            else -> throw IllegalArgumentException("undefined payload type: " + post.type)
        }

}