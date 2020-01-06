package com.roguepnz.memeagg.cluster

import com.mongodb.client.model.*
import com.roguepnz.memeagg.util.Times
import org.bson.BsonDateTime
import org.bson.Document
import org.litote.kmongo.coroutine.CoroutineDatabase
import java.util.concurrent.TimeUnit


class NodeSourceDao(private val config: NodeConfig, db: CoroutineDatabase) {

    private val collection = db.getCollection<Document>("node_source")

    suspend fun insert(source: String) {
        collection.createIndex(
            Indexes.ascending("checkTime"),
            IndexOptions().expireAfter(config.grabbedExpireTimeSec.toLong(), TimeUnit.SECONDS)
        )

        val doc = Document()
            .append("_id", source)
            .append("node", null)
            .append("checkTime", BsonDateTime(System.currentTimeMillis()))

        val options = FindOneAndReplaceOptions()
            .upsert(true)
            .returnDocument(ReturnDocument.AFTER)

        collection.findOneAndReplace(Filters.eq("_id", source), doc, options)
    }

    suspend fun updateGrabbed(node: String) {
        collection.updateMany(Filters.eq("node", node), Updates.set("checkTime", BsonDateTime(System.currentTimeMillis())))
    }

    suspend fun tryGrab(node: String): String? {
        val update = Updates.combine(
            Updates.set("node", node),
            Updates.set("checkTime", Times.now())
        )

        val options = FindOneAndUpdateOptions()
            .upsert(false)
            .returnDocument(ReturnDocument.AFTER)

        val doc = collection.findOneAndUpdate(Filters.eq("node", null), update, options)

        return doc?.getString("_id")
    }
}