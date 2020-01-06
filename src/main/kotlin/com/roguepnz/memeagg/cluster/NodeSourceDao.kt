package com.roguepnz.memeagg.cluster

import com.mongodb.MongoBulkWriteException
import com.mongodb.client.model.*
import com.roguepnz.memeagg.util.Times
import org.bson.BsonDateTime
import org.bson.Document
import org.litote.kmongo.coroutine.CoroutineDatabase
import java.util.concurrent.TimeUnit


class NodeSourceDao(private val config: NodeConfig, db: CoroutineDatabase) {

    private val collection = db.getCollection<Document>("node_source")

    suspend fun insert(sources: List<String>) {
//        collection.createIndex(
//            Indexes.ascending("checkTime"),
//            IndexOptions().expireAfter(config.grabbedExpireTimeSec, TimeUnit.SECONDS)
//        )

        val docs = sources.map {
            Document()
                .append("_id", it)
                .append("node", null)
                .append("checkTime", BsonDateTime(System.currentTimeMillis()))
        }

        val options = InsertManyOptions().ordered(false)

        try {
            collection.insertMany(docs, options)
        } catch (e: MongoBulkWriteException) {
            // TODO mongo insert ignore?
            val ignore = !e.writeErrors.any { it.code != 11000 }
            if (!ignore) {
                throw e
            }
        }
    }

    suspend fun updateGrabbed(node: String) {
        collection.updateMany(Filters.eq("node", node), Updates.set("checkTime", BsonDateTime(System.currentTimeMillis())))
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