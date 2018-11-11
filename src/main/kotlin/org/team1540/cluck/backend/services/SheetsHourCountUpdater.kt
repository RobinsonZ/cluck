package org.team1540.cluck.backend.services

import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.model.ValueRange
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Conditional
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.team1540.cluck.backend.SheetsConfig
import org.team1540.cluck.backend.data.User
import org.team1540.cluck.backend.interfaces.HourCountUpdater
import org.team1540.cluck.backend.interfaces.SheetsProvider
import org.team1540.cluck.backend.testconditional.OfflineConditional


@Service
@Conditional(OfflineConditional::class)
class SheetsHourCountUpdater : HourCountUpdater {
    private val logger = KotlinLogging.logger {}

    @Autowired
    lateinit var config: SheetsConfig

    @Autowired
    lateinit var sheetsProvider: SheetsProvider

    @Async
    override fun setHours(user: User, hourCount: Double) {
        logger.debug { "Setting $user's hour count to $hourCount hours" }
        // find the user's row
        var foundUser = false
        sheetsProvider.sheets.spreadsheets().values().get(config.sheet, config.nameRange).setMajorDimension("COLUMNS").execute()
                .getValues()[0].forEachIndexed { i, value ->
            if (value == user.name) {
                foundUser = true
                sheetsProvider.sheets.spreadsheets().values().update(config.sheet, "${config.hoursCol}${i + config.hoursRowOffset}", ValueRange().setValues(listOf(listOf(hourCount)))).setValueInputOption("RAW").execute()
            }
        }
        if (foundUser) {
            logger.debug { "Successfully set $user's hour count" }
        } else {
            logger.warn { "Attempted to set $user's hour count but could not find them on the spreadsheet" }
        }
    }

    companion object {
        @JvmStatic
        private val JSON_FACTORY = JacksonFactory.getDefaultInstance()
    }
}
