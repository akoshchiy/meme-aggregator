package com.roguepnz.memeagg.source.state

import com.roguepnz.memeagg.db.getCollection
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.reactivestreams.*
import org.litote.kmongo.coroutine.*
import javax.swing.plaf.nimbus.State
import kotlin.reflect.KClass

private const val COLLECTION = "source_state"

class DbStateProvider<T : Any>(private val db: CoroutineDatabase, private val id: String) : StateProvider<T> {

    override suspend fun save(state: T) {
        db.getCollection<StateWrapper<T>>(COLLECTION).save(StateWrapper(id, state))
    }

    override suspend fun get(): T? {
        return db.getCollection<StateWrapper<T>>(COLLECTION).findOneById(id)?.state
    }

    private data class StateWrapper<T : Any>(
        @BsonId val id: String,
        val state: T
    )
}