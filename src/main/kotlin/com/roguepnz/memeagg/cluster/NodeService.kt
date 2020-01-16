package com.roguepnz.memeagg.cluster

import com.roguepnz.memeagg.crawler.ContentCrawler
import com.roguepnz.memeagg.source.ContentSourceBuilder
import com.roguepnz.memeagg.util.Strings
import com.roguepnz.memeagg.util.loggerFor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import java.time.Duration
import kotlin.math.log

class NodeService(private val config: NodeConfig,
                  private val dao: NodeSourceDao,
                  private val builder: ContentSourceBuilder,
                  private val crawler: ContentCrawler) {

    private val logger = loggerFor<NodeService>()
    private val scope = CoroutineScope(Dispatchers.IO)
    private val sources = HashSet<String>()
    private val nodeId: String = Strings.randomAlphaNumeric(6)
    private var remainingSources = config.maxSourcesCount

    fun start() {
        logger.info("starting crawling node: [$nodeId]")
        scope.launch {
            loop()
        }
    }

    private suspend fun loop() {
        try {
            startLoop()
        } catch (e: Exception) {
            logger.error("node service failed", e)
            delay(Duration.ofSeconds(1))
            loop()
        }
    }

    private suspend fun startLoop() {
        dao.insert(builder.sources)
        while (true) {
            dao.updateGrabbed(nodeId, sources)
            if (remainingSources > 0) {
                val grabbed = tryGrab()
                if (!grabbed) {
                    delay(Duration.ofSeconds(config.checkGrabbedSec))
                }
                continue
            } else {
                delay(Duration.ofSeconds(config.checkGrabbedSec))
            }
        }
    }

    private suspend fun tryGrab(): Boolean {
        val sourceId = dao.tryGrab(nodeId)
        if (sourceId != null) {
            startCrawl(sourceId)
        }
        return sourceId != null
    }

    private fun startCrawl(sourceId: String) {
        val source = builder.build(sourceId)
        if (source != null) {
            val config = builder.config(sourceId)
            crawler.crawl(sourceId, config!!.type, source)
            remainingSources -= 1
            sources.add(sourceId)
            logger.info("[$nodeId] grabbed source: $sourceId")
        } else {
            logger.warn("config not found for source: $sourceId")
        }
    }
}