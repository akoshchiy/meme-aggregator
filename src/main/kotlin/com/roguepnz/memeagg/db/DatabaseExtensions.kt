package com.roguepnz.memeagg.db

import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import kotlin.reflect.KClass


fun <T : Any> CoroutineDatabase.getCollection(name: String, type: KClass<T>): CoroutineCollection<T> {
    return this.database.getCollection(name, type.java).coroutine
}