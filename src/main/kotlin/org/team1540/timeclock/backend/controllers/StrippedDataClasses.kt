package org.team1540.timeclock.backend.controllers

import org.team1540.timeclock.backend.data.AccessLevel
import org.team1540.timeclock.backend.data.Credential
import org.team1540.timeclock.backend.data.User

/**
 * A user class stripped of email and clock-in data for sending via APIs.
 */
data class StrippedUser(val name: String, val id: String) {
    constructor(user: User) : this(user.name, user.id)
}

/**
 * A credential class stripped of password data for sending via APIs.
 */
data class StrippedCredential(val username: String, val accessLevel: AccessLevel) {
    constructor(credential: Credential) : this(credential.username, credential.accessLevel)
}
