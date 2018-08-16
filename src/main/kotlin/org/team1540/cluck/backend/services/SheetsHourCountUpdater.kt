package org.team1540.cluck.backend.services

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.health.Health
import org.springframework.context.annotation.Conditional
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.team1540.cluck.backend.config.SheetsConfig
import org.team1540.cluck.backend.data.User
import org.team1540.cluck.backend.interfaces.HourCountUpdater
import org.team1540.cluck.backend.interfaces.SheetsHealthProvider
import org.team1540.cluck.backend.testconditional.OfflineConditional
import java.io.File
import java.io.IOException
import java.util.*
import javax.annotation.PostConstruct


@Service
@Conditional(OfflineConditional::class)
class SheetsHourCountUpdater : HourCountUpdater, SheetsHealthProvider {
    /**
     * Return an indication of health.
     * @return the health for
     */
    override fun getSheetsHealth(): Health {
        return try {
            val start = System.currentTimeMillis()
            val sheetId = sheets.spreadsheets().get(config.sheet).execute().spreadsheetId
            val end = System.currentTimeMillis()
            Health.up().withDetail("sheetID", sheetId).withDetail("ping", end - start).build()
        } catch (e: IOException) {
            Health.down(e).build()
        }
    }

    private val logger = KotlinLogging.logger {}

    @Autowired
    lateinit var config: SheetsConfig

    private lateinit var sheets: Sheets

    @PostConstruct
    private fun init() {
        logger.info { "Initializing Google Sheets API" }
        try {
            val credential = GoogleCredential.fromStream(File(config.serviceFile).inputStream()).createScoped(Collections.singletonList(SheetsScopes.SPREADSHEETS))

            sheets = Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), credential).setApplicationName(config.appName).build()

            // read values

        } catch (e: Exception) {
            logger.error(e) { "Could not initialize Sheets API" }
            throw RuntimeException(e)
        }
        // Run a query to check everything is properly configured
        try {
            val namesResponse = sheets.spreadsheets().values().get(config.sheet, config.nameRange).setMajorDimension("COLUMNS").execute()
            namesResponse.getValues()[0].let {
                if (logger.isTraceEnabled) it.forEach { logger.trace { "Found name: $it" } }

                if (it.isEmpty()) logger.warn { "Found no names in the provided column, check sheet-id and name-range" }
            }
        } catch (e: IOException) {
            logger.warn(e) { "IO error occured when testing Sheets API, something is probably broken" }
        } catch (e: NullPointerException) {
            logger.warn("Recieved unexpected null response from Sheets API, spreadsheet is probably misconfigured")
        }
    }

    @Async
    override fun setHours(user: User, hourCount: Double) {
        logger.debug { "Setting $user's hour count to $hourCount hours" }
        // find the user's row
        var foundUser = false
        sheets.spreadsheets().values().get(config.sheet, config.nameRange).setMajorDimension("COLUMNS").execute()
                .getValues()[0].forEachIndexed { i, value ->
            if (value == user.name) {
                foundUser = true
                sheets.spreadsheets().values().update(config.sheet, "${config.hoursCol}${i + config.hoursRowOffset}", ValueRange().setValues(listOf(listOf(hourCount)))).setValueInputOption("RAW").execute()
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
