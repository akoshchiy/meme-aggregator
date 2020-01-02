package com.roguepnz.memeagg.source.state


interface StateProvider<T : Any> {

    suspend fun save(state: T)

    suspend fun get(): T?

    suspend fun getOrDefault(f: () -> T): T = get() ?: f()
}