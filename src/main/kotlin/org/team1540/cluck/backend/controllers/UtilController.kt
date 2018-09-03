package org.team1540.cluck.backend.controllers

import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UtilController {
    @RequestMapping("/ping")
    fun ping() {

    }

    @GetMapping("authtest")
    fun testAuth(auth: Authentication?): Map<String, String?> {
        return mapOf("accessLevel" to (auth?.authorities?.mapNotNull { it.authority }?.map { it.removePrefix("ROLE_") }?.firstOrNull()
                ?: "NONE"))
    }
}
