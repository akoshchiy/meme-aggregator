package com.roguepnz.memeagg.cluster

import com.roguepnz.memeagg.crawler.ContentCrawler
import com.roguepnz.memeagg.source.ContentSourceBuilder
import com.roguepnz.memeagg.util.Strings
import com.roguepnz.memeagg.util.loggerFor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import java.time.Duration

class NodeService(private val config: NodeConfig,
                  private val dao: NodeSourceDao,
                  private val builder: ContentSourceBuilder,
                  private val crawler: ContentCrawler) {

    private val logger = loggerFor<NodeService>()

    val nodeId: String = Strings.randomAlphaNumeric(6)

    fun start() {
        logger.info("starting crawling node: $nodeId")
        GlobalScope.launch {
            publishSources()
            launch {
                updateGrabbed()
            }
            grabSources()
        }
    }

    private suspend fun publishSources() {
        dao.insert(builder.sources)
//        builder.sources.forEach {
//            dao.insert(it)
//        }
    }

    private suspend fun grabSources() {
        var workers = config.workersCount
        while (workers > 0) {
            val sourceId = dao.tryGrab(nodeId)
            if (sourceId != null) {
                startCrawl(sourceId)
                workers -= 1
            } else {
                delay(Duration.ofSeconds(config.grabDelaySec))
            }
        }
    }

    private fun startCrawl(id: String) {
        val source = builder.build(id)
        logger.info("grabbed source: $id, node: $nodeId")
        crawler.crawl(source)
    }

    private suspend fun updateGrabbed() {
        while (true) {
            dao.updateGrabbed(nodeId)
            delay(Duration.ofSeconds(config.checkGrabbedSec))
        }
    }
}