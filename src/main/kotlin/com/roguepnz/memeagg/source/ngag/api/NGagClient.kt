package com.roguepnz.memeagg.source.ngag.api

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import java.lang.Exception

class NGagClient(private val client: HttpClient) {

    suspend fun getPosts(tag: String, offset: Int): NGagPostResult {
        // TODO handle errors
        val urlTpl = "https://9gag.com/v1/tag-posts/tag/%s/type/fresh?c=%d"
        return client.get(urlTpl.format(tag, offset))
    }
}

data class NGagPostResult(val meta: NGagMeta, val data: NGagPostData)

data class NGagMeta(val timestamp: Int, val status: String, val sid: String)

data class NGagPostData(val posts: List<NGagPost>, val tag: NGagTag, val nextCursor: String?) {

    val nextOffset: Int?
        get() = Integer.parseInt(nextCursor?.substringAfter("c="))

}

data class NGagTag(val key: String, val url: String)

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

//    fun extractPayload(): String =
//        extractUrl(
//            when {
//                isPhoto -> ".jpg"
//                isAnimated -> ".mp4"
//                isVideo -> ".webp"
//                else -> throw IllegalArgumentException("undefined payload type")
//            }
//        )

    fun extractUrl(ext: String): String {
        val url = images.values.asSequence()
            .sortedByDescending { it.width }
            .map { if (ext == ".webp") it.webpUrl ?: "" else it.url }
            .firstOrNull() { it.contains(ext) }
        if (url == null) {
            throw Exception("wtf")
        }
        return url
    }
}

data class NGagPostImage(
    val width: Int,
    val height: Int,
    val url: String,
    val webpUrl: String?
)

//data class NGagCommentResult(val status: String, val error: String, val payload: NGagCommentPayload)
//
//data class NGagCommentPayload(val comments: List<NGagComment>)
//
//data class NGagComment(
//    val commentId: String,
//    val text: String,
//    val timestamp: Int,
//    val orderKey: String?,
//    val likeCount: Int,
//    val dislikeCount: Int,
//    val coinCount: Int
//)



