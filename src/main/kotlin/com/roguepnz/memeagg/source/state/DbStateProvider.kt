package com.roguepnz.memeagg.source.state

import com.roguepnz.memeagg.util.JSON
import org.bson.Document
import org.litote.kmongo.coroutine.CoroutineDatabase
import kotlin.reflect.KClass

class DbStateProvider<T : Any>(db: CoroutineDatabase, private val id: String, private val type: KClass<T>) : StateProvider<T> {

    private val collection = db.getCollection<Document>("source_state")

    override suspend fun save(state: T) {
        collection.save(
            Document()
                .append("_id", id)
                .append("state", state)
        )
    }

    override suspend fun get(): T? {
        val doc = collection.findOneById(id) ?: return null
        val json = doc.get("state", Document::class.java).toJson()
        return JSON.parse(json, type)
    }
}