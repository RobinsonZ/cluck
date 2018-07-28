package org.team1540.timeclock.backend.controllers

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.team1540.timeclock.backend.convertToISODate
import org.team1540.timeclock.backend.data.User
import org.team1540.timeclock.backend.data.UserRepository
import org.team1540.timeclock.backend.interfaces.HoursTracker
import org.team1540.timeclock.backend.services.ClockInOutException

@RestController
class TimeclockController {
    @Autowired
    lateinit var users: UserRepository

    @Autowired
    lateinit var hoursTracker: HoursTracker

    val logger = KotlinLogging.logger {}

    @PostMapping("/clockapi/clock")
    fun clock(@RequestParam user: String, @RequestParam clockingIn: Boolean): Any {
        val time = System.currentTimeMillis()
        return try {
            if (clockingIn) {
                hoursTracker.recordClockIn(user, time)
            } else {
                hoursTracker.recordClockOut(user, time)
            }
            logger.debug { "Successful clock-${if (clockingIn) "in" else "out"} request executed for user $user at time $time (${time.convertToISODate()})" }

            mapOf("time" to time.toString())
        } catch (e: ClockInOutException) {
            logger.debug { "Clock-${if (clockingIn) "in" else "out"} request for user $user at $time (${time.convertToISODate()}) errored due to bad input: ${e.message}" }

            ResponseEntity(mapOf("message" to e.message), HttpStatus.NOT_FOUND)
        }
    }

    @GetMapping("/clockapi/name")
    fun nameForId(@RequestParam id: String): Any {
        logger.debug { "Looking for user with id $id" }
        return users.findById(id).orElse(null)?.let {
            it.also { logger.debug { "Responding with requested user $it" } }.let { StrippedUser(it) }
        }
                ?: ResponseEntity<Any>(HttpStatus.NOT_FOUND).also { logger.debug { "Could not find requested user $id" } }
    }

    @GetMapping("/clockapi/id")
    fun idForName(@RequestParam name: String): Any {
        logger.debug { "Looking for user with name \"$name\"" }
        return users.findByName(name)?.let {
            it.also { logger.debug { "Found requested user $it" } }.let { StrippedUser(it) }
        }
                ?: ResponseEntity<Any>(HttpStatus.NOT_FOUND).also { logger.debug { "Could not find requested user \"$name\"" } }
    }
}

data class StrippedUser(val name: String, val id: String) {
    constructor(user: User) : this(user.name, user.id)
}
