package com.roguepnz.memeagg.crawler

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

private typealias Task = suspend () -> Unit

class CoroutineWorkerPool(private val workers: Int) {

    private val queue = Channel<Task>(Channel.UNLIMITED)

    fun start(scope: CoroutineScope) {
        repeat(workers) {
            scope.launch {
                for (task in queue) {
                    task()
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
}