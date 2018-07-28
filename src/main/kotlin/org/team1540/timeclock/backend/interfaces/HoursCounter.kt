package org.team1540.timeclock.backend.interfaces

import org.springframework.scheduling.annotation.Async
import org.team1540.timeclock.backend.data.User
import java.util.concurrent.CompletableFuture

interface HoursCounter {
    @Async
    fun getTotalMsAsync(user: User): CompletableFuture<Long>

    fun getTotalMs(user: User) = getTotalMsAsync(user).get()
}
