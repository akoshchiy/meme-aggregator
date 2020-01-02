package com.roguepnz.memeagg.source.state

import kotlin.reflect.KClass


interface StateProvider<T : Any> {

    suspend fun save(state: T)

    suspend fun get(type: KClass<T>): T?

    suspend fun getOrDefault(type: KClass<T>, f: () -> T): T = get(type) ?: f()
}