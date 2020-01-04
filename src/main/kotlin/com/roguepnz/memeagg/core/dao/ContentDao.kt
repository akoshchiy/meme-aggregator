package com.roguepnz.memeagg.core.dao

import com.mongodb.client.model.InsertManyOptions
import com.roguepnz.memeagg.core.model.Content
import com.roguepnz.memeagg.core.model.ContentPreview
import org.litote.kmongo.coroutine.CoroutineDatabase

private const val COLLECTION = "content"

class ContentDao(private val db: CoroutineDatabase) {

    suspend fun getById(id: String): Content? {
        return db.getCollection<Content>(COLLECTION).findOneById(id)
    }

    suspend fun insert(batch: List<Content>) {
        val options = InsertManyOptions().ordered(false)
        db.getCollection<Content>(COLLECTION).insertMany(batch, options)
    }

    suspend fun getPreview(): List<ContentPreview> {
        TODO("")
    }
}