package com.roguepnz.memeagg.core.dao

import com.mongodb.client.model.*
import com.roguepnz.memeagg.core.model.Content
import com.roguepnz.memeagg.core.model.ContentPreview
import com.roguepnz.memeagg.core.model.Meta
import com.roguepnz.memeagg.db.Dao
import org.bson.Document
import org.litote.kmongo.coroutine.CoroutineDatabase

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
        return collection.findOneById(id)
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
                    Updates.set("likesCount", it.likesCount),
                    Updates.set("dislikesCount", it.dislikesCount),
                    Updates.set("commentsCount", it.commentsCount)
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

    suspend fun getPage(): List<Content> {
        return collection.find()
            .sort(Sorts.descending("meta.publishTime"))
            .limit(50)
            .toList()
    }


    suspend fun getPreview(): List<ContentPreview> {
        TODO("")
    }
}