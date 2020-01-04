package com.roguepnz.memeagg.source.ngag.api

import com.roguepnz.memeagg.model.ContentType
import com.roguepnz.memeagg.source.model.Payload
import com.roguepnz.memeagg.source.model.RawContent
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import java.lang.Exception

class NGagClient(private val client: HttpClient) {

    suspend fun getByTag(tag: String, cursor: String): NGagContentResult {
        val url = "https://9gag.com/v1/tag-posts/tag/$tag/type/fresh?$cursor"
        val res = client.get<NGagPostResult>(url)
        return extractContent(res)
    }

    suspend fun getByGroup(group: String, cursor: String): NGagContentResult {
        val url = "https://9gag.com/v1/group-posts/group/$group/type/fresh?$cursor"
        val res = client.get<NGagPostResult>(url)
        return extractContent(res)
    }

    private fun extractContent(result: NGagPostResult): NGagContentResult {
        val content = result.data.posts
            .asSequence()
            .filter { it.supported }
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
            .toList()
        return NGagContentResult(content, result.data.nextCursor)
    }

    private fun extractPayload(post: NGagPost): Payload =
        when {
            post.isPhoto -> Payload(ContentType.IMAGE, post.extractUrl(".jpg"))
            post.isVideo -> Payload(ContentType.VIDEO, post.extractUrl(".webp"))
            post.isAnimated -> Payload(ContentType.VIDEO, post.extractUrl(".mp4"))
            else -> throw IllegalArgumentException("undefined payload type: " + post.type)
        }
}

data class NGagContentResult(val content: List<RawContent>, val cursor: String?)

data class NGagPostResult(val meta: NGagMeta, val data: NGagPostData)

data class NGagMeta(val timestamp: Int, val status: String, val sid: String)

data class NGagPostData(val posts: List<NGagPost>, val nextCursor: String?)

data class NGagPost(
    val id: String,
    val url: String,
    val type: String,
    val title: String,
    val upVoteCount: Int,
    val downVoteCount: Int,
    val creationTs: Int,
    val commentsCount: Int,
    val images: Map<String, NGagPostImage>
) {
    val isPhoto: Boolean get() = type == "Photo"
    val isAnimated: Boolean get() = type == "Animated"
    val isVideo: Boolean get() = type == "Video"

    val supported: Boolean get() = isPhoto || isAnimated || isVideo

    fun extractUrl(ext: String): String {
        return images.values.asSequence()
            .sortedByDescending { it.width }
            .map { if (ext == ".webp") it.webpUrl ?: "" else it.url }
            .first { it.contains(ext) }
    }
}

data class NGagPostImage(
    val width: Int,
    val height: Int,
    val url: String,
    val webpUrl: String?
)