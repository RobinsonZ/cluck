package org.team1540.cluck.backend.controllers

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PingController {
    @RequestMapping("/ping")
    fun ping() {

    }
}
