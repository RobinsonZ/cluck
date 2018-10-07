package org.team1540.cluck.backend

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@ConfigurationProperties("cluck.sheets")
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

@ConfigurationProperties("cluck.auth")
@Configuration
class AuthenticationConfig {
    /**
     * Password to authenticate to the admin API, if it has been enabled.
     */
    lateinit var adminPassword: String
}

@ConfigurationProperties("cluck.autologout")
@Configuration
class LogoutConfig {
    /**
     * Whether to enable auto-logout.
     */
    @Value("true")
    var enabled: Boolean = true
}

@ConfigurationProperties("cluck.email")
@Configuration
class EmailConfig {
    /**
     * The from field on automated emails. Defaults to `"CLUCK" <cluck@example.com>`
     */
    @Value("\"CLUCK\" <cluck@example.com>")
    lateinit var emailFrom: String
    /**
     * The reply-to field on automated emails. Defaults to `cluck@example.com`.
     */
    @Value("cluck@example.com")
    lateinit var emailReplyTo: String
}
