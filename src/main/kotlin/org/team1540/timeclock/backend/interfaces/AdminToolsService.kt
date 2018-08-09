package org.team1540.timeclock.backend.interfaces

import org.team1540.timeclock.backend.data.AccessLevel
import org.team1540.timeclock.backend.data.Credential
import org.team1540.timeclock.backend.data.User
import org.team1540.timeclock.backend.services.*

interface AdminToolsService {

    @Throws(UserAlreadyExistsException::class)
    fun addUser(user: User)

    @Throws(NoSuchUserException::class)
    fun removeUser(id: String)

    @Throws(CredentialAlreadyExistsException::class)
    fun addCredentialSet(level: AccessLevel, username: String, password: String)

    @Throws(NoSuchCredentialException::class)
    fun removeCredentialSet(username: String)

    fun getAllCredentials(): Set<Credential>

    fun resetAllHours()

    fun getAllUsers(): Set<UserWithInfo>

    @Throws(UserNotFoundException::class, NeverClockedInException::class, AlreadyClockedInOrOutException::class)
    fun voidLastClock(id: String)

    data class UserWithInfo(val id: String, val name: String, val email: String, val clockedIn: Boolean, val timeIn: Long)
}

