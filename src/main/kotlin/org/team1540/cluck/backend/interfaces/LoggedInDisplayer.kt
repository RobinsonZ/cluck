package org.team1540.cluck.backend.interfaces

import org.springframework.scheduling.annotation.Async

interface LoggedInDisplayer {
    @Async
    fun refreshLoggedInDisplay()
}
