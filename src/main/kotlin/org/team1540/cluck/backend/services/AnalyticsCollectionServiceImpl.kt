package org.team1540.cluck.backend.services

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.team1540.cluck.backend.data.AnalyticsEvent
import org.team1540.cluck.backend.data.AnalyticsRepository
import org.team1540.cluck.backend.interfaces.AnalyticsCollectionService

@Service
class AnalyticsCollectionServiceImpl : AnalyticsCollectionService {
    @Autowired
    private lateinit var analyticsRepository: AnalyticsRepository

    val logger = KotlinLogging.logger {}

    @Async
    override fun recordEvent(timestamp: Long, user: String, description: String) {
        analyticsRepository.save(AnalyticsEvent(timestamp, user, description))
        logger.debug { "Logged event $description" }
    }

}
