package com.roguepnz.memeagg.cluster

import com.roguepnz.memeagg.crawler.ContentCrawler
import com.roguepnz.memeagg.source.ContentSourceBuilder
import com.roguepnz.memeagg.util.Strings
import com.roguepnz.memeagg.util.loggerFor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import java.time.Duration

class NodeService(private val config: NodeConfig,
                  private val dao: NodeSourceDao,
                  private val builder: ContentSourceBuilder,
                  private val crawler: ContentCrawler) {

    private val logger = loggerFor<NodeService>()

    val nodeId: String = Strings.randomAlphaNumeric(6)

    fun start(scope: CoroutineScope) {
        logger.info("starting crawling node: $nodeId")
        scope.launch {
            publishSources()
            launch {
                updateGrabbed()
            }
            grabSources(scope)
        }
    }

    private suspend fun publishSources() {
        dao.insert(builder.sources)
    }

    private suspend fun grabSources(scope: CoroutineScope) {
        var workers = config.workersCount
        while (workers > 0) {
            val sourceId = dao.tryGrab(nodeId)
            if (sourceId != null) {
                startCrawl(scope, sourceId)
                workers -= 1
            } else {
                delay(Duration.ofSeconds(config.grabDelaySec))
            }
        }
    }

    private fun startCrawl(scope: CoroutineScope, sourceId: String) {
        val source = builder.build(sourceId)
        logger.info("grabbed source: $sourceId, node: $nodeId")
        crawler.crawl(scope, sourceId, source)
    }

    private suspend fun updateGrabbed() {
        while (true) {
            dao.updateGrabbed(nodeId)
            delay(Duration.ofSeconds(config.checkGrabbedSec))
        }
    }
}