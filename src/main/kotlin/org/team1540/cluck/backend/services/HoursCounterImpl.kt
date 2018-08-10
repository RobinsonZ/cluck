package org.team1540.cluck.backend.services

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.team1540.cluck.backend.data.TimeCacheEntry
import org.team1540.cluck.backend.data.TimeCacheEntryRepository
import org.team1540.cluck.backend.data.User
import org.team1540.cluck.backend.interfaces.HoursCounter
import java.util.concurrent.CompletableFuture

@Service
class HoursCounterImpl : HoursCounter {

    val logger = KotlinLogging.logger {}
    @Autowired
    private lateinit var timeCacheEntryRepository: TimeCacheEntryRepository

    @Async
    override fun getTotalMsAsync(user: User): CompletableFuture<Long> {
        logger.debug { "Calculating total hours for user ${user.id}" }
        data class HoursAccum(val wasIn: Boolean, val lastTs: Long, val total: Long)
        return user.clockEvents.takeIf { it.size > 1 }
                ?.sortedBy { it.timestamp }
                ?.dropWhile { !it.clockingIn } // remove any clock-out events without clock-ins
                ?.fold(HoursAccum(false, 0, 0)) { acc, event ->
                    if (acc.wasIn == event.clockingIn) {
                        logger.warn {
                            "Two consecutive clock-${if (acc.wasIn) "in" else "out"} records for user " +
                                    "$user at ${acc.lastTs} and ${event.timestamp}. " +
                                    "Data may be corrupted–assuming ${if (acc.wasIn) "later" else "earlier"} time is valid"
                        }
                        acc.copy(lastTs = (if (acc.wasIn) event.timestamp else acc.lastTs))
                    } else {
                        acc.copy(
                                wasIn = event.clockingIn,
                                lastTs = event.timestamp,
                                total = acc.total + if (!event.clockingIn) event.timestamp - acc.lastTs else 0)
                    }
                }?.total
                .also {
                    if (it == null) {
                        logger.warn { "Hours were requested for user $user but they have never clocked in and out" }
                    } else {
                        logger.debug { "User $user has spent $it ms (${(it / 1000.0) / 3600} hrs) clocked in" }
                    }
                }.also {
                    // cache it
                    it?.let { timeCacheEntryRepository.save(TimeCacheEntry(user.id, it)) }
                }.let { CompletableFuture.completedFuture(it ?: 0L) }

    }
}
