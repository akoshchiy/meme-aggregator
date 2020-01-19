package com.roguepnz.memeagg.db

import com.roguepnz.memeagg.Configs
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

object MongoDbBuilder {

    fun build(): CoroutineDatabase {
        val config = Configs.db
        val host = config.getString("host")
        val port = config.getInt("port")
        val db = config.getString("db")

        return KMongo.createClient("mongodb://$host:$port").coroutine.getDatabase(db)
    }
}