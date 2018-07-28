package org.team1540.timeclock.backend.data

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository


@Document(collection = "users")
data class User(@Id val id: String = "", val name: String = "", val email: String = "", val clockEvents: Collection<ClockEvent> = emptyList()) {
    override fun toString() = "$id ($name)"
}

data class ClockEvent(val timestamp: Long = 0L, val clockingIn: Boolean = false)

interface UserRepository : MongoRepository<User, String> {
    fun findByName(name: String): User?
}
