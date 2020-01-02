package com.roguepnz.memeagg.source.state

import com.roguepnz.memeagg.util.JSON
import org.bson.Document
import org.litote.kmongo.coroutine.CoroutineDatabase
import kotlin.reflect.KClass

private const val COLLECTION = "source_state"

class DbStateProvider<T : Any>(private val db: CoroutineDatabase, private val id: String) : StateProvider<T> {

    override suspend fun save(state: T) {
        db.getCollection<Document>(COLLECTION).save(
            Document()
                .append("_id", id)
                .append("state", state)
        )
    }

    override suspend fun get(type: KClass<T>): T? {
        val doc = db.getCollection<Document>(COLLECTION).findOneById(id) ?: return null
        val json = doc.get("state", Document::class.java).toJson()
        return JSON.parse(json, type)
    }
}