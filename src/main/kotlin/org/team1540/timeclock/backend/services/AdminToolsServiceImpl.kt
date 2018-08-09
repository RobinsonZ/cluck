package org.team1540.timeclock.backend.services

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.stereotype.Service
import org.team1540.timeclock.backend.data.*
import org.team1540.timeclock.backend.interfaces.AdminToolsService
import org.team1540.timeclock.backend.interfaces.HourCountUpdater

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
        return credentialRepository.findAll().toSet()
    }

    override fun getAllUsers(): Set<User> {
        logger.debug { "Getting all users" }
        return userRepository.findAll().toSet()
    }

    override fun resetAllHours() {
        logger.info { "Resetting all user hour counts" }
        for (user in userRepository.findAll()) {
            val newUser = userRepository.save(user.copy(clockEvents = emptyList()))
            hourCountUpdater.setHours(newUser, 0.0)
        }
        logger.info { "Reset complete" }
    }

}
