package org.team1540.cluck.backend.interfaces

import org.team1540.cluck.backend.data.User

interface LoggedInUsersTracker {
    /**
     * Gets the currently logged in users. Returns a map of user display name to the time that they clocked in (from the Unix epoch, in milliseconds).
     */
    fun getLoggedInUsers(): Map<String, String>

    fun isUserLoggedIn(user: User): Boolean
}
