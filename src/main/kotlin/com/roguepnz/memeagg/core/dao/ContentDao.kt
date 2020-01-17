package com.roguepnz.memeagg.core.dao

import com.mongodb.client.model.*
import com.roguepnz.memeagg.core.model.*
import com.roguepnz.memeagg.db.Dao
import com.roguepnz.memeagg.feed.api.Feed
import com.roguepnz.memeagg.source.config.SourceType
import org.bson.Document
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineDatabase

class ContentDao(private val db: CoroutineDatabase) : Dao {

    private inline fun <reified T: Any> collection() = db.getCollection<T>("content")

    override suspend fun init() {
        collection<Document>().createIndexes(
            listOf(
                IndexModel(Indexes.descending("order")),
                IndexModel(Indexes.ascending("hash"), IndexOptions().unique(true)),
                IndexModel(Indexes.ascending("rawId"), IndexOptions().unique(true))
            )
        )
    }

    suspend fun requestSeq(count: Int): IntRange {
        val res = db.getCollection<Document>("seq")
            .findOneAndUpdate(
                Filters.eq("_id", "content"),
                Updates.inc("seq", count),
                FindOneAndUpdateOptions()
                    .upsert(true)
                    .returnDocument(ReturnDocument.AFTER)
            )

        val end = res!!.getInteger("seq")

        return IntRange(end - count + 1, end)
    }

    suspend fun getById(id: String): Content? {
        return collection<Content>().findOneById(ObjectId(id))
    }

    suspend fun getViewById(id: String): ContentView? {
        val c = collection<Content>().findOneById(ObjectId(id)) ?: return null
        return ContentView(
            c.id!!,
            c.rawId,
            ContentType.fromCode(c.contentType),
            c.url,
            c.hash,
            SourceType.fromCode(c.sourceType),
            c.sourceId,
            c.publishTime,
            c.likesCount,
            c.dislikesCount,
            c.commentsCount,
            c.rating,
            c.author,
            c.title
        )
    }

    suspend fun insert(batch: List<Content>) {
        val options = BulkWriteOptions()
            .ordered(false)

        val updates = batch.map {
            UpdateOneModel<Document>(
                Filters.eq("hash", it.hash),
                Updates.combine(
                    Updates.setOnInsert(
                        Document()
                            .append("contentType", it.contentType)
                            .append("insertSeq", it.insertSeq)
                            .append("hash", it.hash)
                            .append("url", it.url)
                            .append("rawId", it.rawId)
                            .append("sourceId", it.sourceId)
                            .append("sourceType", it.sourceType)
                            .append("author", it.author)
                            .append("rating", it.rating)
                            .append("title", it.title)
                            .append("publishTime", it.publishTime)
                            .append("likesCount", it.likesCount)
                            .append("dislikesCount", it.dislikesCount)
                            .append("commentsCount", it.commentsCount)
                            .append("order", it.order)
                    )
                ),
                UpdateOptions().upsert(true)
            )
        }

        collection<Document>().bulkWrite(updates, options)
    }

    suspend fun update(batch: List<ContentUpdate>) {
        val updates = batch.map {
            UpdateOneModel<Content>(
                Filters.eq("rawId", it.rawId),
                Updates.combine(
                    Updates.set("likesCount", it.likesCount),
                    Updates.set("dislikesCount", it.dislikesCount),
                    Updates.set("commentsCount", it.commentsCount),
                    Updates.set("rating", it.rating)
                )
            )
        }

        collection<Content>().bulkWrite(updates, BulkWriteOptions().ordered(false))
    }

    suspend fun containsBySource(keys: List<String>): Set<String> {
        return collection<Document>()
            .find(Filters.`in`("rawId", keys))
            .projection(Projections.include("_id", "rawId"))
            .toList()
            .asSequence()
            .map { it.getString("rawId") }
            .toSet()
    }

    suspend fun getFeed(count: Int, after: Int? = null): Feed {
        var query = collection<Document>()
            .find()
            .sort(Sorts.descending("order"))
            .limit(count)
            .projection(Projections.include("contentType", "url"))

        if (after != null) {
            query = query.filter(Filters.lt("order", after))
        }

        val items = query.toList()
            .map {
                ContentPreview(
                    it.getObjectId("_id").toHexString(),
                    ContentType.fromCode(it.getInteger("contentType")),
                    it.getString("url")
                )
            }
        return Feed(items)
    }
}