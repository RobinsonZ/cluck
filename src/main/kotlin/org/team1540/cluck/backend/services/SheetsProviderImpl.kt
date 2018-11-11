package org.team1540.cluck.backend.services

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.team1540.cluck.backend.SheetsConfig
import org.team1540.cluck.backend.interfaces.SheetsProvider
import java.io.File
import java.io.IOException
import java.util.*
import javax.annotation.PostConstruct

@Service
class SheetsProviderImpl : SheetsProvider {
    override lateinit var sheets: Sheets

    @Autowired
    lateinit var config: SheetsConfig

    private val logger = KotlinLogging.logger {}

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
            logger.warn("Received unexpected null response from Sheets API, spreadsheet is probably misconfigured")
        }
    }
}
