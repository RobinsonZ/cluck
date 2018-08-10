package org.team1540.cluck.backend.interfaces

import org.team1540.cluck.backend.data.User

interface HourCountUpdater {
    fun setHours(user: User, hourCount: Double)
}
