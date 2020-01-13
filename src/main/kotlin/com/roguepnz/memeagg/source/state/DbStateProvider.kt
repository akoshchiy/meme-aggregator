package com.roguepnz.memeagg.source.state

import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import com.roguepnz.memeagg.util.JSON
import org.bson.Document
import org.litote.kmongo.coroutine.CoroutineDatabase
import kotlin.reflect.KClass

class DbStateProvider<T : Any>(db: CoroutineDatabase, private val id: String) : StateProvider<T> {

    private val collection = db.getCollection<Document>("source_state")

    override suspend fun save(state: T, ver: Int): Boolean {
        val update = Updates.combine(
            Updates.set("state", state),
            Updates.inc("ver", ver + 1)
        )

        val filter = Filters.and(
            Filters.eq("_id", id),
            Filters.eq("ver", ver)
        )

        val options = UpdateOptions().upsert(true)
        val result = collection.updateOne(filter, update, options)

        return result.modifiedCount > 0
    }

    override suspend fun get(type: KClass<T>): Pair<T, Int>? {
        val doc = collection.findOneById(id) ?: return null

        val json = doc.get("state", Document::class.java).toJson()
        val state = JSON.parse(json, type)
        val ver = doc.getInteger("ver")

        return Pair(state, ver)
    }
}