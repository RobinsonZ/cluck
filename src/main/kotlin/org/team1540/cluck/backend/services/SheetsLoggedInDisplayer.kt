package org.team1540.cluck.backend.services

import com.google.api.services.sheets.v4.model.ValueRange
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.team1540.cluck.backend.SheetsConfig
import org.team1540.cluck.backend.data.UserRepository
import org.team1540.cluck.backend.interfaces.LoggedInDisplayer
import org.team1540.cluck.backend.interfaces.LoggedInUsersTracker
import org.team1540.cluck.backend.interfaces.SheetsProvider
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Service
class SheetsLoggedInDisplayer : LoggedInDisplayer {
    @Autowired
    lateinit var config: SheetsConfig
    @Autowired
    lateinit var sheetsProvider: SheetsProvider
    @Autowired
    lateinit var loggedInUsersTracker: LoggedInUsersTracker
    @Autowired
    lateinit var users: UserRepository

    private val logger = KotlinLogging.logger {}

    private val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

    @Async
    override fun refreshLoggedInDisplay() {
        // construct our users list
        // sortBy orders users so that users who are logged in come first; then we put blank for logged-out users
        // this ensures that users are properly removed when they log out because we write all the data each time
        logger.debug { "Updating logged-in display" }
        val usersValueRange = ValueRange().apply {
            setValues(users.findAll()
                    .sortedBy { if (loggedInUsersTracker.isUserLoggedIn(it)) 0 else 1 }
                    .map {
                        if (loggedInUsersTracker.isUserLoggedIn(it)) {
                            listOf(it.name, formatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(it.lastEvent
                                    ?: 0), ZoneId.systemDefault())))
                        } else {
                            listOf("", "")
                        }
                    }
                    .sortedWith(Comparator { first, second ->
                        when {
                            first[0] == "" -> 1
                            second[0] == "" -> -1
                            else -> first[0].compareTo(second[0], ignoreCase = true)
                        }
                    }))
        }
        sheetsProvider.sheets.spreadsheets().values().update(config.sheet, "'${config.loggedInSheetName}'!A2", usersValueRange).setValueInputOption("RAW").execute()
        logger.debug { "Updated logged-in display" }
    }
}
