package com.roguepnz.memeagg.core.dao

import com.mongodb.client.model.Indexes
import com.mongodb.client.model.InsertManyOptions
import com.mongodb.client.model.Sorts
import com.roguepnz.memeagg.core.model.Content
import com.roguepnz.memeagg.core.model.ContentPreview
import org.litote.kmongo.coroutine.CoroutineDatabase

private const val COLLECTION = "content"

class ContentDao(private val db: CoroutineDatabase) {

    suspend fun getById(id: String): Content? {
        return db.getCollection<Content>(COLLECTION).findOneById(id)
    }

    suspend fun insert(batch: List<Content>) {
        val collection = db.getCollection<Content>(COLLECTION)

        collection.ensureIndex(Indexes.descending("meta.publishTime"))

        val options = InsertManyOptions().ordered(false)
        collection.insertMany(batch, options)
    }

    suspend fun getPage(): List<Content> {
        val collection = db.getCollection<Content>(COLLECTION)

        return collection.find()
            .sort(Sorts.descending("meta.publishTime"))
            .limit(50)
            .toList()
    }

    suspend fun getPreview(): List<ContentPreview> {
        TODO("")
    }
}