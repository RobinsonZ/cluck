package org.team1540.cluck.backend.services

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

open class AdminToolsException(message: String, val httpStatus: HttpStatus) : Exception(message) {
    val responseEntity get() = ResponseEntity(mapOf("message" to message), httpStatus)
}

class NoSuchUserException : AdminToolsException("not_found", HttpStatus.NOT_FOUND)
class UserAlreadyExistsException : AdminToolsException("already_exists", HttpStatus.BAD_REQUEST)
class CredentialAlreadyExistsException : AdminToolsException("cred_already_exists", HttpStatus.BAD_REQUEST)
class NoSuchCredentialException : AdminToolsException("cred_not_found", HttpStatus.NOT_FOUND)

open class ClockInOutException(message: String) : Exception(message)
class UserNotFoundException : ClockInOutException("not_found")
class AlreadyClockedInOrOutException : ClockInOutException("repeat_clock")
class NeverClockedInException : ClockInOutException("never_clocked")
