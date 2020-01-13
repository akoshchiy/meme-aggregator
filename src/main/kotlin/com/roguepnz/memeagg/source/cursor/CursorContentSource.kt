package com.roguepnz.memeagg.source.cursor

import com.roguepnz.memeagg.source.ContentSource
import com.roguepnz.memeagg.source.model.RawContent
import com.roguepnz.memeagg.source.model.RawMeta
import com.roguepnz.memeagg.source.state.StateProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import java.time.Duration



data class CursorState(var crawlCursor: Cursor?,
                       var crawledCount: Int,
                       var updateCursor: Cursor?,
                       var updateCounter: Int,
                       var lastPostTime: Int)

class CursorContentSource(private val cursorProvider: CursorProvider,
                          private val stateProvider: StateProvider,
                          bufSize: Int,
                          private val checkCount: Int) : ContentSource {

    private val contentChannel: Channel<RawContent> = Channel(bufSize)
    private val metaChannel: Channel<RawMeta> = Channel(bufSize)

    override fun start(scope: CoroutineScope) {
        scope.launch { startCrawl() }
        scope.launch { startUpdate() }
    }

    override fun contentChannel() = contentChannel
    override fun metaChannel() = metaChannel

    private suspend fun startCrawl() {
        while (true) {
            val state = getState()

            val cursor = state.first.crawlCursor
            val content = if (cursor != null) cursorProvider.next(cursor) else cursorProvider.next()

            if (content.data.isEmpty()) {
                break
            }

            content.data.forEach {
                contentChannel.send(it)
            }

            state.first.crawlCursor = content.cursor
            if (state.first.lastPostTime == 0) {
                state.first.lastPostTime =  content.data[0].publishTime
            }

            trySave(state.first, state.second) {
                if (it.first.lastPostTime == 0) {
                    it.first.lastPostTime = content.data[0].publishTime
                }
                state.first.crawlCursor = content.cursor
            }

            if (!content.cursor.hasNext) {
                break
            }
        }
    }

    private suspend fun startUpdate() {
        // TODO stop?
        while (true) {
            delay(Duration.ofMinutes(1))
            checkUpdate()
        }
    }

    private suspend fun checkUpdate() {
        val state = stateProvider.getOrDefault("up", CursorUpdateState::class) { CursorUpdateState() }

        while (state.count < checkCount) {
            val content = if (state.cursor != null) cursorProvider.next(state.cursor!!) else cursorProvider.next()

            state.count += content.data.size
            state.cursor = content.cursor

            content.data.forEach {
                // TODO
//                metaChannel.send(it.)
            }

            stateProvider.save("up", state)

            if (!content.cursor.hasNext) {
                break
            }
        }
        stateProvider.save("up", CursorUpdateState())
    }

    private suspend fun checkNew() {
//        val state = getState()

        var state: NewState = stateProvider.get(NewState::class) ?: return
        var cursor: Cursor? = null

        while (true) {
            val content = if (cursor == null) cursorProvider.next() else cursorProvider.next(cursor)

            val filtered = content.data.asSequence()
                .sortedBy { it.publishTime }
                .filter { it.publishTime > state.lastPostTime }
                .toList()

            if (filtered.isEmpty()) {
                return
            }







        }
    }

    private data class CrawlState(var cursor: Cursor?, var crawledCount: Int)
    private data class NewState(var lastPostTime: Int)
    private data class MetaState(var cursor: Cursor?, var counter: Int)

//    private suspend fun getState(): Pair<CursorState, Int> {
//        return stateProvider.getOrDefault(CursorState::class) {
//            CursorState(
//                null,
//                0,
//                null,
//                0,
//                0
//            )
//        }
//    }/

//    private suspend fun trySave(state: CursorState, ver: Int, onFail: (Pair<CursorState, Int>) -> Unit) {
//        stateProvider.trySave(state, ver, CursorState::class, onFail)
//    }

}