package org.team1540.cluck.backend.interfaces

import org.team1540.cluck.backend.services.ClockInOutException

/**
 * A service that can clock in or out users.
 */
interface HoursTracker {
    /**
     * Record a user with id [id] clocking in at [timeMs] milliseconds after the Unix epoch.
     *
     * @throws org.team1540.cluck.backend.services.UserNotFoundException if the user does not exist
     * @throws org.team1540.cluck.backend.services.AlreadyClockedInOrOutException if the user has already clocked in without clocking out
     */
    @Throws(ClockInOutException::class)
    fun recordClockIn(id: String, timeMs: Long)

    /**
     * Record a user with id [id] clocking in at [timeMs] milliseconds after the Unix epoch.
     *
     * @throws UserNotFoundException if the user does not exist.
     * @throws AlreadyClockedInOrOutException if the user has already clocked out without clocking in again
     * @throws NeverClockedInException if the user has never clocked in
     */
    @Throws(ClockInOutException::class)
    fun recordClockOut(id: String, timeMs: Long)
}
