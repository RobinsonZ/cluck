package org.team1540.cluck.backend.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Service
import org.team1540.cluck.backend.interfaces.SheetsHealthProvider
import org.team1540.cluck.backend.testconditional.OfflineConditional

@Service
@Conditional(OfflineConditional::class)
class SheetsHealthIndicator : HealthIndicator {
    @Autowired
    private lateinit var sheetsHourCountUpdater: SheetsHealthProvider

    override fun health() = sheetsHourCountUpdater.getSheetsHealth()
}
