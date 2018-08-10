package org.team1540.cluck.backend.services

import mu.KotlinLogging
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.team1540.cluck.backend.data.User
import org.team1540.cluck.backend.interfaces.HourCountUpdater

@Service
@Profile("offline")
class MockHourCountUpdater : HourCountUpdater {
    private val logger = KotlinLogging.logger {}

    override fun setHours(user: User, hourCount: Double) {
        logger.debug { "Setting user $user hour count to $hourCount" }
    }

}
