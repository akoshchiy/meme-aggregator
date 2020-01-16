package com.roguepnz.memeagg.cluster

import com.mongodb.client.model.*
import com.roguepnz.memeagg.db.Dao
import org.bson.BsonDateTime
import org.bson.Document
import org.litote.kmongo.coroutine.CoroutineDatabase


class NodeSourceDao(private val config: NodeConfig, db: CoroutineDatabase) : Dao {

    private val collection = db.getCollection<Document>("node_source")

    suspend fun insert(sources: List<String>) {
        val options = BulkWriteOptions().ordered(false)
        val updates = sources.map {
            UpdateOneModel<Document>(
                Filters.eq("_id", it),
                Updates.setOnInsert(
                    Document()
                        .append("_id", it)
                        .append("node", null)
                        .append("checkTime", BsonDateTime(System.currentTimeMillis()))
                ),
                UpdateOptions().upsert(true)
            )
        }
        collection.bulkWrite(updates, options)
    }

    suspend fun updateGrabbed(node: String, sources: Iterable<String>) {
        collection.updateMany(
            Filters.and(
                Filters.eq("node", node),
                Filters.`in`("_id", sources)
            ),
            Updates.set("checkTime", BsonDateTime(System.currentTimeMillis()))
        )
    }

    suspend fun tryGrab(node: String): String? {
        val update = Updates.combine(
            Updates.set("node", node),
            Updates.set("checkTime", BsonDateTime(System.currentTimeMillis()))
        )

        val options = FindOneAndUpdateOptions()
            .upsert(false)
            .returnDocument(ReturnDocument.AFTER)

        val checkTime = BsonDateTime(System.currentTimeMillis() - config.grabbedExpireTimeSec * 1000)

        val filter = Filters.or(
            Filters.eq("node", null),
            Filters.lt("checkTime", checkTime)
        )

        val doc = collection.findOneAndUpdate(filter, update, options)

        return doc?.getString("_id")
    }
}