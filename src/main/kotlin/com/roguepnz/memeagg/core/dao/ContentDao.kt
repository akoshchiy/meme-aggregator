package com.roguepnz.memeagg.core.dao

import com.mongodb.client.model.Indexes
import com.mongodb.client.model.InsertManyOptions
import com.mongodb.client.model.Sorts
import com.roguepnz.memeagg.core.model.Content
import com.roguepnz.memeagg.core.model.ContentPreview
import org.litote.kmongo.coroutine.CoroutineDatabase


class ContentDao(db: CoroutineDatabase) {

    private val collection = db.getCollection<Content>("content")

    suspend fun getById(id: String): Content? {
        return collection.findOneById(id)
    }

    suspend fun insert(batch: List<Content>) {
        collection.createIndex(Indexes.descending("meta.publishTime"))
        collection.insertMany(batch, InsertManyOptions().ordered(false))
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