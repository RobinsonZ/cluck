package org.team1540.timeclock.backend.interfaces

import org.team1540.timeclock.backend.data.User

interface HourCountUpdater {
    fun setHours(user: User, hourCount: Double)
}
