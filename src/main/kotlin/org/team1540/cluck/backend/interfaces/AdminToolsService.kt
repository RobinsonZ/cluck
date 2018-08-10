package org.team1540.cluck.backend.interfaces

import org.team1540.cluck.backend.data.AccessLevel
import org.team1540.cluck.backend.data.Credential
import org.team1540.cluck.backend.data.User
import org.team1540.cluck.backend.services.*

/**
 * A service that can provide cluck administration tools.
 */
interface AdminToolsService {

    /**
     * Add the provided [user] object to the database.
     * @throws UserAlreadyExistsException Ff another user in the database has the same ID.
     */
    @Throws(UserAlreadyExistsException::class)
    fun addUser(user: User)

    /**
     * Remove the user with the specified [id] from the database.
     * @throws NoSuchUserException if there is no such user.
     */
    @Throws(NoSuchUserException::class)
    fun removeUser(id: String)

    /**
     * Add a credential set to the database, with the provided [level], [username], and [password]. The password should
     * be unencoded.
     *
     * @throws CredentialAlreadyExistsException if a credential with the specified username already exists in the database.
     */
    @Throws(CredentialAlreadyExistsException::class)
    fun addCredentialSet(level: AccessLevel, username: String, password: String)

    /**
     * Remove the credential set with the specified [username] from the database.
     * @throws NoSuchCredentialException if there is no such credential.
     */
    @Throws(NoSuchCredentialException::class)
    fun removeCredentialSet(username: String)

    /**
     * Get all credentials currently in the database.
     */
    fun getAllCredentials(): Set<Credential>

    /**
     * Remove all clock records from all users, and update hour counts accordingly.
     */
    fun resetAllHours()

    /**
     * Gets all users currently in the database, as well as providing some extra info via the use of the [UserWithInfo] class.
     */
    fun getAllUsers(): Set<UserWithInfo>

    /**
     * Clocks a user out without incrementing their hour count.
     */
    @Throws(UserNotFoundException::class, NeverClockedInException::class, AlreadyClockedInOrOutException::class)
    fun voidLastClock(id: String)

    /**
     * Data class to include user data for sending via the API, as well as additional info.
     */
    data class UserWithInfo(
            val id: String, val name: String, val email: String,
            /** Whether or not the user is currently clocked in. */
            val clockedIn: Boolean,
            /** The time this user has spent clocked in, in milliseconds.*/
            val timeIn: Long)
}

