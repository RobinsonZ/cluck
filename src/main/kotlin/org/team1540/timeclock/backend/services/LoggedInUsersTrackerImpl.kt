package org.team1540.timeclock.backend.services

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.team1540.timeclock.backend.data.User
import org.team1540.timeclock.backend.data.UserRepository
import org.team1540.timeclock.backend.interfaces.LoggedInUsersTracker

@Service
class LoggedInUsersTrackerImpl : LoggedInUsersTracker {

    private val logger = KotlinLogging.logger {}

    @Autowired
    lateinit var users: UserRepository

    override fun getLoggedInUsers(): Map<String, String> {
        // TODO: Find a better way of doing this than iterating through every user
        logger.debug { "Processing request for logged in users" }
        return users.findAll()
                .filter { it.clockEvents.sortedBy { it.timestamp }.lastOrNull()?.clockingIn ?: false }
                .associate { it.name to it.clockEvents.last().timestamp.toString() }
                .also { logger.debug { "${it.size} logged-in users found" } }
    }

    override fun isUserLoggedIn(user: User) = user.clockEvents.sortedBy { it.timestamp }.last().clockingIn
}
