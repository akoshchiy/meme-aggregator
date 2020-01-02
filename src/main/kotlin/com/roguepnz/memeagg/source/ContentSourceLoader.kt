package com.roguepnz.memeagg.source

import com.roguepnz.memeagg.source.ngag.NGagContentSource
import com.roguepnz.memeagg.source.ngag.NGagSourceConfig
import com.roguepnz.memeagg.source.ngag.api.NGagClient
import io.ktor.client.HttpClient

class ContentSourceLoader(private val httpClient: HttpClient) {

    fun getSources(): List<ContentSource> {
        // TODO read from config
        return listOf(
            NGagContentSource(NGagSourceConfig("german"), NGagClient(httpClient))
        )
    }
}