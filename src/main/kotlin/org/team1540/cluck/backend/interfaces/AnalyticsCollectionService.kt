package org.team1540.cluck.backend.interfaces

/**
 * A service that can provide data collection functionality.
 */
interface AnalyticsCollectionService {
    /**
     * Log an event for data collection. [timestamp] should be a UNIX epoch timestamp in milliseconds, [user] should be
     * the name of the user initiating the event, and [description] should be a short (1-2 word) description of the event.
     */
    fun recordEvent(timestamp: Long, user: String, description: String)
}
