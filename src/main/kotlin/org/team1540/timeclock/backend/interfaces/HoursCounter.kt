package org.team1540.timeclock.backend.interfaces

import org.springframework.scheduling.annotation.Async
import org.team1540.timeclock.backend.data.User
import java.util.concurrent.CompletableFuture

/**
 * A service that can calculate the total hours of a user.
 */
interface HoursCounter {
    @Async
    fun getTotalMsAsync(user: User): CompletableFuture<Long>

    fun getTotalMs(user: User) = getTotalMsAsync(user).get()
}
