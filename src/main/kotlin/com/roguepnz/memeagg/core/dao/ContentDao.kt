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
                IndexModel(Indexes.ascending("sourceKey"), IndexOptions().unique(true))
            )
        )
    }

    suspend fun getById(id: String): Content? {
        return collection.findOneById(id)
    }

    suspend fun save(batch: List<Content>) {
        val options = BulkWriteOptions()
            .ordered(false)

        val updates = batch.map {
            UpdateOneModel<Content>(
                Filters.eq("hash", it.hash),
                Updates.combine(
                    Updates.inc("sourcesCount", 1),
                    Updates.setOnInsert(
                        Document()
                            .append("sourceKey", it.sourceKey)
                            .append("type", it.contentType)
                            .append("hash", it.hash)
                            .append("url", it.url)
                            .append("publishTime", it.publishTime)
                            .append("likesCount", it.likesCount)
                            .append("dislikesCount", it.dislikesCount)
                            .append("commentsCount", it.commentsCount)
                    )
                ),
                UpdateOptions().upsert(true)
            )
        }

        collection.bulkWrite(updates, options)
    }

    suspend fun contains(sourceKeys: List<String>): Set<String> {
        return docCollection
            .find(Filters.`in`("sourceKey", sourceKeys))
            .projection(Projections.include("_id", "sourceKey"))
            .toList()
            .asSequence()
            .map { it.getString("sourceKey") }
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