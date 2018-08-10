package org.team1540.cluck.backend.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.team1540.cluck.backend.interfaces.LoggedInUsersTracker

@RestController
class TimesheetController {
    @Autowired
    lateinit var usersTracker: LoggedInUsersTracker

    @GetMapping("/timesheet/loggedin")
    fun getLoggedInUsers() = usersTracker.getLoggedInUsers()

}
