package com.roguepnz.memeagg.crawler

import com.roguepnz.memeagg.util.loggerFor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select

class BatchWorker<T>(private val queueSize: Int,
                     private val waitTimeSec: Int,
                     private val workFun: suspend (CoroutineScope, List<T>) -> Unit) {

    private val logger = loggerFor<BatchWorker<T>>()

    private val channel: Channel<T> = Channel()
    private val batch: MutableList<T> = ArrayList()

    fun start(scope: CoroutineScope) {
        scope.launch {
            loop(this)
        }
    }

    suspend fun add(item: T) {
        channel.send(item)
    }

    private suspend fun loop(scope: CoroutineScope) {
        var deadline = 0L
        while (true) {
            val remainingTime = deadline - System.currentTimeMillis()

            if (batch.isNotEmpty() && remainingTime <= 0 || batch.size >= queueSize) {
                try {
                    workFun(scope, batch)
                } catch (e: Exception) {
                    logger.error("batch work failed", e)
                }
                batch.clear()
                continue
            }

            select<Unit> {
                channel.onReceive {
                    batch.add(it)
                    if (batch.size == 1) {
                        deadline = System.currentTimeMillis() + waitTimeSec * 1000L
                    }
                }
                if (batch.isNotEmpty()) {
                    onTimeout(remainingTime){}
                }
            }
        }
    }
}