package org.team1540.cluck.backend.controllers

import mu.KotlinLogging
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UtilController {

    val logger = KotlinLogging.logger {}

    @RequestMapping("/ping")
    fun ping() {

    }

    @GetMapping("authtest")
    fun testAuth(auth: Authentication?): Map<String, String?> {
        val authLevel = (auth?.authorities?.mapNotNull { it.authority }?.map { it.removePrefix("ROLE_") }?.firstOrNull()
                ?: "NONE")
        logger.debug {
            if (auth == null) {
                "Received authentication test request from unauthenticated user (access level $authLevel)"
            } else {
                "Received authentication test request from user ${auth.name} (access level $authLevel)"
            }
        }
        return mapOf("accessLevel" to authLevel)
    }
}
