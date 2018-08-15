package org.team1540.cluck.backend.services

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.stereotype.Service
import org.team1540.cluck.backend.convertToISODate
import org.team1540.cluck.backend.data.*
import org.team1540.cluck.backend.interfaces.AdminToolsService
import org.team1540.cluck.backend.interfaces.HourCountUpdater
import org.team1540.cluck.backend.interfaces.HoursCounter

@Service
class AdminToolsServiceImpl : AdminToolsService {
    private val encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

    private val logger = KotlinLogging.logger { }

    @Autowired
    private lateinit var credentialRepository: CredentialRepository
    @Autowired
    private lateinit var userRepository: UserRepository
    @Autowired
    private lateinit var hourCountUpdater: HourCountUpdater
    @Autowired
    private lateinit var timeCacheEntryRepository: TimeCacheEntryRepository
    @Autowired
    private lateinit var hoursCounter: HoursCounter

    override fun addCredentialSet(level: AccessLevel, username: String, password: String) {
        logger.debug { "Adding new credential set \"$username\" with access level $level" }
        if (!credentialRepository.existsById(username)) {
            val encodedPassword = encoder.encode(password)
            credentialRepository.save(Credential(level, username, encodedPassword))
            logger.debug { "Created new credential set \"$username\"" }
        } else {
            logger.debug { "Failed to create new credential; credential with username \"$username\" already exists" }
            throw CredentialAlreadyExistsException()
        }
    }


    override fun removeCredentialSet(username: String) {
        logger.debug { "Deleting credential \"$username\"" }
        if (userRepository.existsById(username)) {
            userRepository.deleteById(username)
            logger.debug { "Deleted credential \"$username\"" }
        } else {
            logger.debug { "Failed to delete credential with username \"$username\": no such credential exists" }
            throw NoSuchCredentialException()
        }
    }

    override fun addUser(user: User) {
        logger.debug { "Adding user $user" }
        if (!userRepository.existsById(user.id)) {
            userRepository.save(user)
            logger.debug { "Saved user $user" }
        } else {
            logger.debug { "Failed to add user $user: ID ${user.id} is already assigned to user ${userRepository.findById(user.id).orElse(null)}" }
            throw UserAlreadyExistsException()
        }
    }

    override fun removeUser(id: String) {
        logger.debug { "Deleting user with ID $id" }
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id)
            logger.debug { "Deleted user with ID $id" }
        } else {
            logger.debug { "Failed to delete user with ID $id: no such user exists" }
            throw NoSuchUserException()
        }
    }

    override fun getAllCredentials(): Set<Credential> {
        logger.debug { "Getting all credentials" }
        val credentials = credentialRepository.findAll().toSet()
        logger.debug { "Found ${credentials.size} credentials" }
        return credentials
    }

    override fun getAllUsers(): Set<AdminToolsService.UserWithInfo> {
        logger.debug { "Getting all users" }
        val users = userRepository.findAll().toSet()
        logger.debug { "Found ${users.size} users" }

        return users.map { user ->
            // check the cache; if we get a cache miss recalculate
            AdminToolsService.UserWithInfo(
                    user.id,
                    user.name,
                    user.email,
                    user.clockEvents.sortedBy { it.timestamp }.lastOrNull()?.clockingIn ?: false,
                    timeCacheEntryRepository.findById(user.id).orElse(null)?.time
                            ?: run {
                                logger.debug { "Cache miss for user ${user.id}, recalculating hours" }
                                hoursCounter.getTotalMs(user)
                            }
            )
        }.toSet()
    }

    override fun resetAllHours() {
        logger.info { "Resetting all user hour counts" }
        for (user in userRepository.findAll()) {
            val newUser = userRepository.save(user.copy(clockEvents = emptyList()))
            hourCountUpdater.setHours(newUser, 0.0)
        }
        logger.info { "Reset complete" }
    }

    override fun voidLastClock(id: String) {
        logger.debug { "Voiding last clock-in for user $id" }
        val user: User? = userRepository.findById(id).orElse(null)
        if (user == null) {
            logger.debug { "User $id was not found" }
            throw UserNotFoundException()
        }

        val lastClockEvent = user.clockEvents.sortedBy { it.timestamp }.lastOrNull()
        if (lastClockEvent?.clockingIn == false) {
            // already clocked in
            logger.debug { "User $id already clocked out at time ${lastClockEvent.timestamp} (${lastClockEvent.timestamp.convertToISODate()})" }
            throw AlreadyClockedInOrOutException()
        } else if (lastClockEvent == null) {
            logger.debug { "User $id never clocked in" }
            throw NeverClockedInException()
        }

        userRepository.save(user.copy(clockEvents = user.clockEvents.sortedBy { it.timestamp }.dropLast(1)))
        logger.debug { "Voided last clock-in for user $user" }
    }

    /**
     * Edits a user's data. If any parameters are `null`, then they keep their old value in the database.
     */
    override fun editUser(id: String, newId: String?, newName: String?, newEmail: String?) {
        logger.debug {
            "Modifying user info for user $id${if (newId != null) "; changing ID to $newId" else ""}${if (newName != null) "; changing name to \"$newEmail\"" else ""}${if (newEmail != null) "; changing email to $newEmail" else ""}"
        }

        if (!userRepository.existsById(id)) {
            logger.debug { "Could not find user $id to modify" }
            throw NoSuchUserException()
        }

        val oldUser = userRepository.findById(id).orElse(null)!!

        userRepository.save(oldUser.copy(id = newId ?: oldUser.id, name = newName ?: oldUser.name, email = newEmail
                ?: oldUser.email))
        logger.debug { "Successfully edited user ${newId ?: id}" }

        if (newId != null) {
            userRepository.deleteById(oldUser.id)
            logger.debug { "Deleted old user instance" }
        }

    }

}
