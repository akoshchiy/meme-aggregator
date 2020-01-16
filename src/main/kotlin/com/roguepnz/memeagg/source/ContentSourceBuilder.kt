package com.roguepnz.memeagg.source

import com.roguepnz.memeagg.source.config.ContentSourceConfig
import com.roguepnz.memeagg.source.config.SourceType
import com.roguepnz.memeagg.source.cursor.CursorState
import com.roguepnz.memeagg.source.debeste.DebesteConfig
import com.roguepnz.memeagg.source.debeste.DebesteContentSource
import com.roguepnz.memeagg.source.debeste.DebesteState
import com.roguepnz.memeagg.source.ngag.tag.NGagTagContentSource
import com.roguepnz.memeagg.source.ngag.tag.NGagTagConfig
import com.roguepnz.memeagg.source.ngag.NGagClient
import com.roguepnz.memeagg.source.ngag.group.NGagGroupConfig
import com.roguepnz.memeagg.source.ngag.group.NGagGroupContentSource
import com.roguepnz.memeagg.source.reddit.RedditClient
import com.roguepnz.memeagg.source.reddit.RedditConfig
import com.roguepnz.memeagg.source.reddit.RedditContentSource
import com.roguepnz.memeagg.source.state.DbStateProvider
import com.roguepnz.memeagg.util.UrlDownloader
import com.typesafe.config.Config
import io.ktor.client.HttpClient
import org.litote.kmongo.coroutine.CoroutineDatabase

class ContentSourceBuilder(config: Config, private val http: HttpClient, private val db: CoroutineDatabase) {

    private val configs: Map<String, ContentSourceConfig> = readConfig(config)

    private fun readConfig(c: Config): Map<String, ContentSourceConfig> {
        return c.getConfigList("sources")
            .asSequence()
            .map { ContentSourceConfig(it) }
            .map { Pair(it.id, it) }
            .toMap()
    }

    fun build(id: String): ContentSource? {
        val config = config(id)
        if (config != null) {
            return build(config)
        }
        return null
    }

    fun config(id: String): ContentSourceConfig? {
        return if (configs.containsKey(id)) configs[id] else null
    }

    private fun build(config: ContentSourceConfig): ContentSource {
        return when(config.type) {
            SourceType.NGAG_TAG -> {
                NGagTagContentSource(
                    NGagTagConfig(config.config),
                    NGagClient(http),
                    DbStateProvider(db, config.id, CursorState::class)
                )
            }
            SourceType.NGAG_GROUP -> {
                NGagGroupContentSource(
                    NGagGroupConfig(config.config),
                    NGagClient(http),
                    DbStateProvider(db, config.id, CursorState::class)
                )
            }
            SourceType.REDDIT -> {
                val conf = RedditConfig(config.config)
                RedditContentSource(
                    conf,
                    RedditClient(conf, http),
                    DbStateProvider(db, config.id, CursorState::class)
                )
            }
            SourceType.DEBESTE -> {
                val conf = DebesteConfig(config.config)
                DebesteContentSource(
                    conf,
                    DbStateProvider(db, config.id, DebesteState::class),
                    UrlDownloader(conf.maxConcurrentDownloads, http)
                )
            }

            else -> throw IllegalArgumentException("unsupported source type: " + config.type)
        }
    }

    val sources: List<String> get() = configs.keys.toList()
}