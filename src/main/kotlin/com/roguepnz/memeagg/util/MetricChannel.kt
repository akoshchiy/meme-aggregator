package com.roguepnz.memeagg.util

import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelIterator
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.ValueOrClosed
import kotlinx.coroutines.selects.SelectClause1
import kotlinx.coroutines.selects.SelectClause2
import java.lang.UnsupportedOperationException
import java.util.concurrent.atomic.AtomicInteger

class MetricChannel<T>(private val channel: Channel<T>, private val metricSize: AtomicInteger) : Channel<T> {

    @ExperimentalCoroutinesApi
    override val isClosedForReceive: Boolean
        get() = channel.isClosedForReceive

    @ExperimentalCoroutinesApi
    override val isClosedForSend: Boolean
        get() = channel.isClosedForSend

    @ExperimentalCoroutinesApi
    override val isEmpty: Boolean
        get() = channel.isEmpty

    @ExperimentalCoroutinesApi
    override val isFull: Boolean
        get() = throw UnsupportedOperationException()

    override val onReceive: SelectClause1<T>
        get() = channel.onReceive

    @InternalCoroutinesApi
    override val onReceiveOrClosed: SelectClause1<ValueOrClosed<T>>
        get() = channel.onReceiveOrClosed

    @ObsoleteCoroutinesApi
    override val onReceiveOrNull: SelectClause1<T?>
        get() = channel.onReceiveOrNull

    override val onSend: SelectClause2<T, SendChannel<T>>
        get() = channel.onSend

    override fun cancel(cause: Throwable?): Boolean {
        throw UnsupportedOperationException()
    }

    override fun cancel(cause: CancellationException?) {
        channel.cancel(cause)
    }

    override fun close(cause: Throwable?): Boolean {
        return channel.close(cause)
    }

    @ExperimentalCoroutinesApi
    override fun invokeOnClose(handler: (cause: Throwable?) -> Unit) {
        channel.invokeOnClose(handler)
    }

    override fun iterator(): ChannelIterator<T> {
        return MetricChannelIterator(metricSize, channel.iterator())
    }

    override fun offer(element: T): Boolean {
        val res = channel.offer(element)
        if (res) {
            metricSize.incrementAndGet()
        }
        return res
    }

    override fun poll(): T? {
        val item = channel.poll()
        if (item != null) {
            metricSize.decrementAndGet()
        }
        return item
    }

    override suspend fun receive(): T {
        val item = channel.receive()
        metricSize.decrementAndGet()
        return item
    }

    @InternalCoroutinesApi
    override suspend fun receiveOrClosed(): ValueOrClosed<T> {
        val res = channel.receiveOrClosed()
        if (!res.isClosed) {
            metricSize.decrementAndGet()
        }
        return res
    }

    @ObsoleteCoroutinesApi
    override suspend fun receiveOrNull(): T? {
        val item = channel.receiveOrNull()
        if (item != null) {
            metricSize.decrementAndGet()
        }
        return item
    }

    override suspend fun send(element: T) {
        channel.send(element)
        metricSize.incrementAndGet()
    }

    private class MetricChannelIterator<E>(private val size: AtomicInteger, private val it: ChannelIterator<E>) : ChannelIterator<E> {

        override suspend fun hasNext(): Boolean {
            return it.hasNext()
        }

        override fun next(): E {
            val item = it.next()
            size.decrementAndGet()
            return item
        }
    }
}