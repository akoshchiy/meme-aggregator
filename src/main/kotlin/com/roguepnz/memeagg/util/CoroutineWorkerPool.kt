package com.roguepnz.memeagg.util

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

private typealias Task = suspend () -> Unit

class CoroutineWorkerPool(workers: Int) {

    private val logger = loggerFor<CoroutineWorkerPool>()
    private val queue = Channel<Task>(Channel.UNLIMITED)
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        repeat(workers) {
            scope.launch {
                for (task in queue) {
                    try {
                        task()
                    } catch (e: Exception) {
                        logger.error("worker task failed", e)
                    }
                }
            }
        }
    }

    fun <T> submitAsync(action: suspend () -> T): Deferred<T> {
        val deferred = CompletableDeferred<T>()
        queue.offer {
            val res = action()
            deferred.complete(res)
        }
        return deferred
    }

    suspend fun <T> submit(action: suspend () -> T): T {
        return submitAsync(action).await()
    }
}