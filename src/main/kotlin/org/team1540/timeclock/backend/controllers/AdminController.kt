package org.team1540.timeclock.backend.controllers

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.team1540.timeclock.backend.data.AccessLevel
import org.team1540.timeclock.backend.data.User
import org.team1540.timeclock.backend.interfaces.AdminToolsService
import org.team1540.timeclock.backend.services.AdminToolsException
import org.team1540.timeclock.backend.services.ClockInOutException

@RestController
class AdminController {
    @Autowired
    private lateinit var adminToolsService: AdminToolsService
    private val logger = KotlinLogging.logger { }

    data class AddCredentialRequestBody(var accessLevel: String, var username: String, var password: String)

    @PostMapping("/admin/addcredential")
    fun addCredential(@RequestBody credential: AddCredentialRequestBody) = doServiceAction {
        try {
            AccessLevel.valueOf(credential.accessLevel)
        } catch (e: IllegalArgumentException) {
            logger.debug { "Could not parse access level \"${credential.accessLevel}\"" }
            null
        }?.let {
            adminToolsService.addCredentialSet(it, credential.username, credential.password)
            ResponseEntity.ok().build<Any>()
        } ?: ResponseEntity.badRequest().build<Any>()
    }

    @PostMapping("/admin/removecredential")
    fun removeCredential(@RequestParam username: String) = doServiceAction {
        adminToolsService.removeCredentialSet(username)
        ResponseEntity.ok().build<Any>()
    }

    @PostMapping("/admin/adduser")
    fun addUser(@RequestParam id: String, @RequestParam name: String, @RequestParam email: String) = doServiceAction {
        ResponseEntity.ok(adminToolsService.addUser(User(id, name, email)))
    }

    @PostMapping("/admin/removeuser")
    fun removeUser(@RequestParam id: String) = doServiceAction {
        ResponseEntity.ok(adminToolsService.removeUser(id))
    }

    @GetMapping("/admin/allcredentials")
    fun getAllCredentials() = mapOf("credentials" to adminToolsService.getAllCredentials().map { StrippedCredential(it) })

    @GetMapping("/admin/allusers")
    fun getAllUsers() = mapOf("users" to adminToolsService.getAllUsers())

    @PostMapping("/admin/reset")
    fun resetAllHours() {
        adminToolsService.resetAllHours()
    }

    @PostMapping("/admin/voidclock")
    fun void(@RequestParam id: String): ResponseEntity<Any> {
        return try {
            adminToolsService.voidLastClock(id)
            ResponseEntity.ok().build<Any>()
        } catch (e: ClockInOutException) {
            logger.debug { "Void request for user $id errored due to bad input: ${e.message}" }

            ResponseEntity(mapOf("message" to e.message), HttpStatus.NOT_FOUND)
        }
    }

    private fun <R : ResponseEntity<*>> doServiceAction(action: () -> R): ResponseEntity<*> {
        return try {
            action()
        } catch (e: AdminToolsException) {
            e.responseEntity
        }
    }
}
