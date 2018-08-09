package org.team1540.timeclock.backend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@ConfigurationProperties("timeclock.sheets")
@Configuration
class SheetsConfig {
    /**
     * The name of the Sheets service account JSON.
     */
    lateinit var serviceFile: String
    /**
     * The app name.
     */
    lateinit var appName: String
    /**
     * The sheet ID of the spreadsheet to update.
     */
    lateinit var sheet: String
    /**
     * The sheet range (e.g. A1:A20) containing the names of users.
     */
    lateinit var nameRange: String
    /**
     * The column of the spreadsheet that the app should update.
     */
    lateinit var hoursCol: String
    /**
     * The row at which hours cells start. For instance, if your spreadsheet has two rows of headers
     * before the hours records, this would be 3.
     */
    var hoursRowOffset: Int = 0
}

@ConfigurationProperties("timeclock.auth")
@Configuration
class AuthenticationConfig {
    /**
     * The username of the timeclock (the client must have this username)
     */
    lateinit var timeclockUsername: String
    /**
     * The password of the timeclock
     */
    lateinit var timeclockPassword: String
    /**
     * Whether to secure the timesheet (i.e. logged-in users) API.
     */
    @Value("false")
    var secureTimesheetApi: Boolean = false
    /**
     * Username to authenticate to the timesheet API, if it has been enabled.
     */
    @Value("")
    lateinit var timesheetUsername: String
    /**
     * Password to authenticate to the timesheet API, if it has been enabled.
     */
    @Value("")
    lateinit var timesheetPassword: String
    /**
     * Password to authenticate to the admin API, if it has been enabled.
     */
    lateinit var adminPassword: String
}

@ConfigurationProperties("timeclock.autologout")
@Configuration
class LogoutConfig {
    /**
     * Whether to enable auto-logout.
     */
    @Value("true")
    var enabled: Boolean = true
}
