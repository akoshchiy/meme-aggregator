package com.roguepnz.meme.source.ngag.api

import com.roguepnz.meme.model.Post
import io.ktor.client.HttpClient
import io.ktor.client.request.get


class NGagClient(private val client: HttpClient) {

    suspend fun getPosts(tag: String, offset: Int): NGagPostResult {
        // TODO handle errors
        val urlTpl = "https://9gag.com/v1/tag-posts/tag/%s/type/fresh?c=%d"
        return client.get(urlTpl.format(tag, offset))
    }

//    suspend fun getComments(postId: String): NGagCommentResult {
//        val urlTpl =
//            "https://comment-cdn.9gag.com/v1/cacheable/comment-list.json?appId=a_dd8f2b7d304a10edaf6f29517ea0ca4100a43d1b&url=http:%2F%2F9gag.com%2Fgag%2FaPR80yP&count=100&order=ts&origin=https:%2F%2F9gag.com"
//
//
//
//
//        TODO()
//
//
//    }

    private fun mapResult(res: NGagPostResult): List<Post> {
        res.data.posts.asSequence()
            .map {


            }


//        for (post in res.data.posts) {
//            post.
//
//        }

        TODO("later")
    }
}

data class NGagPostResult(val meta: NGagMeta, val data: NGagPostData)

data class NGagMeta(val timestamp: Int, val status: String, val sid: String)

data class NGagPostData(val posts: List<NGagPost>, val tag: NGagTag, val nextCursor: String)

data class NGagTag(val key: String, val url: String)

data class NGagPost(
    val id: String,
    val url: String,
    val title: String,
    val upVoteCount: Int,
    val downVoteCount: Int,
    val creationTs: Int,
    val commentsCount: Int,
    val images: Map<String, NGagPostImage>
)

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



