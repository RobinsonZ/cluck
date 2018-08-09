package org.team1540.timeclock.backend.interfaces

import org.team1540.timeclock.backend.data.AccessLevel
import org.team1540.timeclock.backend.data.Credential
import org.team1540.timeclock.backend.data.User
import org.team1540.timeclock.backend.services.CredentialAlreadyExistsException
import org.team1540.timeclock.backend.services.NoSuchCredentialException
import org.team1540.timeclock.backend.services.NoSuchUserException
import org.team1540.timeclock.backend.services.UserAlreadyExistsException

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

    data class UserWithInfo(val id: String, val name: String, val email: String, val clockedIn: Boolean, val timeIn: Long)
}

