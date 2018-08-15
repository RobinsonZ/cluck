package org.team1540.cluck.backend.services

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.team1540.cluck.backend.convertToISODate
import org.team1540.cluck.backend.data.ClockEvent
import org.team1540.cluck.backend.data.User
import org.team1540.cluck.backend.data.UserRepository
import org.team1540.cluck.backend.interfaces.HourCountUpdater
import org.team1540.cluck.backend.interfaces.HoursCounter
import org.team1540.cluck.backend.interfaces.HoursTracker

@Service
class HoursTrackerImpl : HoursTracker {

    @Autowired
    lateinit var users: UserRepository

    @Autowired
    lateinit var hoursUpdater: HourCountUpdater

    @Autowired
    lateinit var hoursCounter: HoursCounter

    val logger = KotlinLogging.logger {}

    override fun recordClockIn(id: String, timeMs: Long) {
        logger.debug { "Processing clock-in request for user $id at time $timeMs (${timeMs.convertToISODate()})" }
        val user: User? = users.findById(id).orElse(null)
        if (user == null) {
            logger.debug { "User $id was not found" }
            throw UserNotFoundException()
        }


        val alreadyClockedIn = user.inNow ?: user.clockEvents.sortedBy { it.timestamp }.lastOrNull()?.clockingIn
        ?: false
        if (alreadyClockedIn) {
            // already clocked in
            logger.debug { "User $id already clocked in without clocking out again" }
            if (user.inNow == null) {
                users.save(user.copy(inNow = true))
            }
            throw AlreadyClockedInOrOutException()
        }

        users.save(user.copy(clockEvents = user.clockEvents + ClockEvent(timeMs, true), inNow = true, lastEvent = timeMs))
        logger.debug { "Recorded clock-in for user $id at time $timeMs (${timeMs.convertToISODate()})" }
    }

    override fun recordClockOut(id: String, timeMs: Long) {
        logger.debug { "Processing clock-out request for user $id at time $timeMs (${timeMs.convertToISODate()})" }
        val user: User? = users.findById(id).orElse(null)
        if (user == null) {
            logger.debug { "User $id was not found" }
            throw UserNotFoundException()
        }

        val alreadyClockedIn = user.inNow ?: user.clockEvents.sortedBy { it.timestamp }.lastOrNull()?.clockingIn
        ?: false
        if (!alreadyClockedIn) {
            // already clocked in
            logger.debug { "User $id already clocked out without clocking in again" }
            if (user.inNow == null) {
                users.save(user.copy(inNow = false))
            }
            throw AlreadyClockedInOrOutException()
        }


        val savedUser = users.save(user.copy(clockEvents = user.clockEvents + ClockEvent(timeMs, false), inNow = false, lastEvent = timeMs))
        hoursCounter.getTotalMsAsync(savedUser).thenAccept {
            hoursUpdater.setHours(savedUser, (it / 1000.0) / 3600.0) // convert milliseconds to hours
            logger.debug { "Clocked out user $id and updated hour count to $it ms (${(it / 1000.0) / 3600.0} hrs)" }
        }
    }
}

