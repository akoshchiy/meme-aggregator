package com.roguepnz.memeagg.source

import com.roguepnz.memeagg.source.ngag.NGagContentSource
import com.roguepnz.memeagg.source.ngag.NGagSourceConfig
import com.roguepnz.memeagg.source.ngag.NGagState
import com.roguepnz.memeagg.source.ngag.api.NGagClient
import com.roguepnz.memeagg.source.state.DbStateProvider
import io.ktor.client.HttpClient
import org.litote.kmongo.coroutine.CoroutineDatabase

class ContentSourceLoader(private val httpClient: HttpClient, private val db: CoroutineDatabase) {

    fun getSources(): List<ContentSource> {
        // TODO read from config
        return listOf(
            NGagContentSource(NGagSourceConfig("german"), NGagClient(httpClient), DbStateProvider(db, "9gag:german"))
        )
    }
}