package org.team1540.cluck.backend.controllers

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.web.bind.annotation.*
import org.team1540.cluck.backend.data.*
import org.team1540.cluck.backend.interfaces.HourCountUpdater
import org.team1540.cluck.backend.interfaces.HoursCounter

@RestController
class AdminController {
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

    private val encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

    private val logger = KotlinLogging.logger { }

    /**
     * Special data class for the request body so we don't encode the password in the URL like we would most parameters.
     */
    data class AddCredentialRequestBody(var accessLevel: String, var username: String, var password: String)

    @PostMapping("/admin/addcredential")
    fun addCredential(@RequestBody credential: AddCredentialRequestBody): ResponseEntity<*> {
        try {
            val it = AccessLevel.valueOf(credential.accessLevel)
            logger.debug { "Adding new credential set \"${credential.username}\" with access level $it" }
            return if (!credentialRepository.existsById(credential.username)) {
                val encodedPassword = encoder.encode(credential.password)
                credentialRepository.save(Credential(it, credential.username, encodedPassword))
                logger.debug { "Created new credential set \"${credential.username}\"" }
                ResponseEntity.ok().build<Any>()
            } else {
                logger.debug { "Failed to create new credential; credential with username \"${credential.username}\" already exists" }
                ResponseEntity.badRequest().body(mapOf("message" to "cred_already_exists"))
            }
        } catch (e: IllegalArgumentException) {
            logger.debug { "Could not parse access level \"${credential.accessLevel}\"" }
            return ResponseEntity.badRequest().build<Any>()
        }
    }

    @PostMapping("/admin/removecredential")
    fun removeCredential(@RequestParam username: String): ResponseEntity<*> {
        logger.debug { "Deleting credential \"$username\"" }
        return if (credentialRepository.existsById(username)) {
            credentialRepository.deleteById(username)
            logger.debug { "Deleted credential \"$username\"" }
            ResponseEntity.ok().build<Any>()
        } else {
            logger.debug { "Failed to delete credential with username \"$username\": no such credential exists" }
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("message" to "cred_not_found"))
        }
    }

    @PostMapping("/admin/adduser")
    fun addUser(@RequestParam id: String, @RequestParam name: String, @RequestParam email: String): ResponseEntity<*> {
        logger.debug { "Adding user $id ($name)" }
        return if (!userRepository.existsById(id)) {
            userRepository.save(User(id, name, email))
            logger.debug { "Saved user $id ($name)" }
            ResponseEntity.ok().build<Any>()
        } else {
            logger.debug { "Failed to add user $id ($name): ID $id is already assigned to user ${userRepository.findById(id).orElse(null)}" }
            ResponseEntity.badRequest().body(mapOf("message" to "already_exists"))
        }
    }

    @PostMapping("/admin/removeuser")
    fun removeUser(@RequestParam id: String): ResponseEntity<*> {
        logger.debug { "Deleting user with ID $id" }
        return if (userRepository.existsById(id)) {
            userRepository.deleteById(id)
            logger.debug { "Deleted user with ID $id" }
            ResponseEntity.ok().build<Any>()
        } else {
            logger.debug { "Failed to delete user with ID $id: no such user exists" }
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("message" to "not_found"))
        }
    }

    @GetMapping("/admin/allcredentials")
    fun getAllCredentials(): Map<String, List<StrippedCredential>> {
        logger.debug { "Getting all credentials" }
        val creds = mapOf("credentials" to credentialRepository.findAll().toSet().map { StrippedCredential(it) })
        logger.debug { "Found ${creds.size} credentials" }
        return creds
    }

    /**
     * Data class to include user data for sending via the API, as well as additional info.
     */
    private data class UserWithInfo(
            val id: String, val name: String, val email: String,
            /** Whether or not the user is currently clocked in. */
            val clockedIn: Boolean,
            /** The time this user has spent clocked in, in milliseconds.*/
            val timeIn: Long,
            /** The time the user last clocked in or out. */
            val lastEventTime: String)

    @GetMapping("/admin/allusers")
    fun getAllUsers(): Map<*, *> {
        logger.debug { "Getting all users" }
        val users = userRepository.findAll().toSet()
        logger.debug { "Found ${users.size} users" }

        // this is not commenting on the economic state of a given user but rather creating a "rich" user object with
        // data not stored alongside the user
        val richUsers = users.map { user ->
            // check the cache; if we get a cache miss recalculate
            UserWithInfo(
                    user.id,
                    user.name,
                    user.email,
                    user.inNow ?: user.clockEvents.maxBy { it.timestamp }?.clockingIn ?: false,
                    timeCacheEntryRepository.findById(user.id).orElse(null)?.time
                            ?: run {
                                logger.debug { "Cache miss for user ${user.id}, recalculating hours" }
                                hoursCounter.getTotalMs(user)
                            },
                    (user.lastEvent ?: user.clockEvents.maxBy { it.timestamp }?.timestamp
                    ?: 0L).toString()
            )
        }.toSet()
        logger.debug { "Indexed all users" }
        return mapOf("users" to richUsers)
    }

    @PostMapping("/admin/reset")
    fun resetAllHours() {
        logger.info { "Resetting all user hour counts" }
        for (user in userRepository.findAll()) {
            val newUser = userRepository.save(user.copy(clockEvents = emptyList(), inNow = false, lastEvent = null))
            hourCountUpdater.setHours(newUser, 0.0)
        }
        timeCacheEntryRepository.deleteAll()
        logger.info { "Reset complete" }
    }

    @PostMapping("/admin/voidclock")
    fun void(@RequestParam id: String): ResponseEntity<Any> {
        logger.debug { "Voiding last clock-in for user $id" }
        val user: User? = userRepository.findById(id).orElse(null)
        if (user == null) {
            logger.debug { "User $id was not found" }
            return ResponseEntity(mapOf("message" to "not_found"), HttpStatus.NOT_FOUND)
        }

        val alreadyClockedIn = user.inNow ?: user.clockEvents.maxBy { it.timestamp }?.clockingIn
        ?: false
        if (!alreadyClockedIn) {
            // already clocked out
            logger.debug { "User $id already clocked out" }
            return ResponseEntity(mapOf("message" to "repeat_clock"), HttpStatus.NOT_FOUND)
        }

        val sortedEvents = user.clockEvents.sortedBy { it.timestamp }

        userRepository.save(user.copy(clockEvents = sortedEvents.dropLast(1), inNow = false, lastEvent = sortedEvents.lastOrNull()?.timestamp))
        logger.debug { "Voided last clock-in for user $user" }

        return ResponseEntity.ok().build<Any>()
    }

    @PostMapping("/admin/edituser")
    fun editUser(@RequestParam id: String, @RequestParam newId: String?, @RequestParam newName: String?, @RequestParam newEmail: String?): ResponseEntity<*> {
        logger.debug {
            "Modifying user info for user $id${if (newId != null) "; changing ID to $newId" else ""}${if (newName != null) "; changing name to \"$newName\"" else ""}${if (newEmail != null) "; changing email to $newEmail" else ""}"
        }

        if (!userRepository.existsById(id)) {
            logger.debug { "Could not find user $id to modify" }
            return ResponseEntity(mapOf("message" to "not_found"), HttpStatus.BAD_REQUEST)
        }

        if (newId != null && userRepository.existsById(newId)) {
            logger.debug { "User with ID $newId already exists" }
            return ResponseEntity(mapOf("message" to "already_exists"), HttpStatus.BAD_REQUEST)
        }

        val oldUser = userRepository.findById(id).orElse(null)!!

        userRepository.save(oldUser.copy(id = newId ?: oldUser.id, name = newName ?: oldUser.name, email = newEmail
                ?: oldUser.email))
        logger.debug { "Successfully edited user ${newId ?: id}" }

        if (newId != null) {
            userRepository.deleteById(oldUser.id)
            logger.debug { "Deleted old user instance" }
        }
        return ResponseEntity.ok().build<Any>()
    }

    @GetMapping("/admin/userhistory")
    fun getUserHistory(@RequestParam id: String): ResponseEntity<*> {
        logger.debug { "Getting user history for user $id" }

        val user = userRepository.findById(id).orElse(null)
                ?: return ResponseEntity(mapOf("message" to "not_found"), HttpStatus.NOT_FOUND).also {
                    logger.debug { "Could not find user $id" }
                }
        logger.debug { "Found ${user.clockEvents.size} events for user $user" }

        val hist = user.clockEvents.sortedBy { it.timestamp }.map { mapOf("timestamp" to it.timestamp.toString(), "clockingIn" to it.clockingIn) }
        return ResponseEntity.ok(mapOf("events" to hist))
    }
}
