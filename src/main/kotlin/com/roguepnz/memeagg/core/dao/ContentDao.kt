package com.roguepnz.memeagg.core.dao

import com.mongodb.client.model.*
import com.roguepnz.memeagg.core.model.Content
import com.roguepnz.memeagg.core.model.ContentPreview
import com.roguepnz.memeagg.core.model.ContentType
import com.roguepnz.memeagg.core.model.Meta
import com.roguepnz.memeagg.db.Dao
import com.roguepnz.memeagg.feed.api.Feed
import org.bson.Document
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.id.ObjectIdGenerator

class ContentDao(private val db: CoroutineDatabase) : Dao {

    private val collection = db.getCollection<Content>("content")
    private val docCollection = db.getCollection<Document>("content")

    override suspend fun init() {
        collection.createIndexes(
            listOf(
                IndexModel(Indexes.descending("publishTime")),
                IndexModel(Indexes.ascending("hash"), IndexOptions().unique(true)),
                IndexModel(Indexes.ascending("meta.sourceKey"), IndexOptions().unique(true))
            )
        )
    }

    suspend fun getById(id: String): Content? {
        return collection.findOneById(ObjectId(id))
    }

    suspend fun insert(batch: List<Content>) {
        val options = BulkWriteOptions()
            .ordered(false)

        val updates = batch.map {
            UpdateOneModel<Content>(
                Filters.eq("hash", it.hash),
                Updates.combine(
                    Updates.setOnInsert(
                        Document()
                            .append("type", it.contentType)
                            .append("hash", it.hash)
                            .append("url", it.url)
                            .append("meta", Document()
                                .append("sourceKey", it.meta.sourceKey)
                                .append("publishTime", it.meta.publishTime)
                                .append("likesCount", it.meta.likesCount)
                                .append("dislikesCount", it.meta.dislikesCount)
                                .append("commentsCount", it.meta.commentsCount)
                            )

                    )
                ),
                UpdateOptions().upsert(true)
            )
        }

        collection.bulkWrite(updates, options)
    }

    suspend fun updateMeta(batch: List<Meta>) {
        val updates = batch.map {
            UpdateOneModel<Content>(
                Filters.eq("meta.sourceKey", it.sourceKey),
                Updates.combine(
                    Updates.set("meta.publishTime", it.publishTime),
                    Updates.set("meta.likesCount", it.likesCount),
                    Updates.set("meta.dislikesCount", it.dislikesCount),
                    Updates.set("meta.commentsCount", it.commentsCount)
                )
            )
        }

        collection.bulkWrite(updates, BulkWriteOptions().ordered(false))
    }

    suspend fun contains(sourceKeys: List<String>): Set<String> {
        return docCollection
            .find(Filters.`in`("meta.sourceKey", sourceKeys))
            .projection(Projections.include("_id", "meta.sourceKey"))
            .toList()
            .asSequence()
            .map { it.get("meta", Document::class.java).getString("sourceKey") }
            .toSet()
    }

    suspend fun getFeedByTime(count: Int, afterPublishTime: Int?): Feed {
        var query = docCollection
            .find()
            .sort(Sorts.descending("meta.publishTime"))
            .limit(count)
            .projection(Projections.include("type", "url", "meta.publishTime"))

        if (afterPublishTime != null) {
            query = query.filter(Filters.lt("meta.publishTime", afterPublishTime))
        }

        val docs = query.toList()
        if (docs.isEmpty()) {
            return Feed(ArrayList(), null)
        }

        val cursor = docs[docs.size - 1].get("meta", Document::class.java).getInteger("publishTime")

        return Feed(
            docs.map {
                ContentPreview(
                    it.getObjectId("_id").toHexString(),
                    ContentType.fromCode(it.getInteger("type")),
                    it.getString("url")
                )
            },
            cursor.toString()
        )
    }
}