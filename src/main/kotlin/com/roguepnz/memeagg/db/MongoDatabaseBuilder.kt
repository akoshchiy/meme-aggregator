package com.roguepnz.memeagg.db

import com.mongodb.MongoClientSettings
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.*

object MongoDatabaseBuilder {

    fun build(): CoroutineDatabase {
        return KMongo.createClient("mongodb://localhost:27017").coroutine.getDatabase("memedb")
    }
}