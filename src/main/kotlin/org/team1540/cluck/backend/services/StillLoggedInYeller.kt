package org.team1540.cluck.backend.services

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.mail.MailException
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.team1540.cluck.backend.data.SimpleEmail
import org.team1540.cluck.backend.data.UserRepository
import org.team1540.cluck.backend.interfaces.EmailService

/**
 * Service to email users if they do not clock out on a certain day.
 */
@Service
@ConditionalOnProperty("cluck.autologout.enabled")
class StillLoggedInYeller {
    val logger = KotlinLogging.logger {}
    @Autowired
    private lateinit var users: UserRepository
    @Autowired
    private lateinit var emailService: EmailService

    @Scheduled(cron = "59 59 23 * * ?") // 11:59:59 PM every day
    fun checkOutstandingLogins() {
        val start = System.currentTimeMillis()
        logger.debug { "Processing outstanding logins" }

        try {
            emailService.send(*users.findAll()
                    .associate {
                        it to it.clockEvents.sortedBy { it.timestamp }.lastOrNull()
                    }
                    .filter { it.value?.clockingIn == true }
                    .map {
                        // delete that last entry from the database
                        users.save(it.key.copy(clockEvents = it.key.clockEvents - it.value!!))
                        SimpleEmail(it.key.email, "Lab Hours", "You didn't sign out of the lab today; these hours have been lost.")
                    }.toTypedArray())
        } catch (e: MailException) {
            logger.error(e) { "Failed to send emails" }
        }

        logger.info { "Oustanding logins processed in ${System.currentTimeMillis() - start} ms" }
    }

}
