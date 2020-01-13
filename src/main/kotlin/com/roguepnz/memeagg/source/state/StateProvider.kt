package com.roguepnz.memeagg.source.state

import kotlin.reflect.KClass


interface StateProvider {

    suspend fun <T : Any> save(state: T, ver: Int): Boolean

    suspend fun <T : Any> get(type: KClass<T>): T?

    suspend fun <T : Any> getOrDefault(type: KClass<T>, f: () -> T): T = get(type) ?: f()

//    suspend fun trySave(state: T, ver: Int, type: KClass<T>, onFail: (Pair<T, Int>) -> Unit) {
//        val succeed = save(state, ver)
//        if (!succeed) {
//            val actual = get(type)!!
//            onFail(actual)
//            trySave(actual.first, actual.second, type, onFail)
//        }
//    }
}