package org.team1540.timeclock.backend.interfaces

import org.team1540.timeclock.backend.data.Session
import org.team1540.timeclock.backend.data.User
import org.team1540.timeclock.backend.services.NoSuchUserException

interface AdminToolsService {
    fun addUser(user: User)

    @Throws(NoSuchUserException::class)
    fun removeUser(id: String)

    fun getHoursForUser(user: User): Long

    fun getAllHours(): Map<User, Long>

    fun getUserInfo(id: String): User

    fun voidLastClock(id: String)

    fun getAllSessions(id: String): List<Session>
}

