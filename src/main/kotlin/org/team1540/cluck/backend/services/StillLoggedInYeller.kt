package org.team1540.cluck.backend.services

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.mail.MailException
import org.springframework.mail.MailSender
import org.springframework.mail.SimpleMailMessage
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.team1540.cluck.backend.EmailConfig
import org.team1540.cluck.backend.convertToISODate
import org.team1540.cluck.backend.data.UserRepository
import org.team1540.cluck.backend.interfaces.AnalyticsCollectionService

/**
 * Service to email users if they do not clock out on a certain day.
 */
@Service
@ConditionalOnProperty(value = ["cluck.autologout.enabled"], matchIfMissing = true)
class StillLoggedInYeller {
    val logger = KotlinLogging.logger {}
    @Autowired
    private lateinit var users: UserRepository
    @Autowired
    private lateinit var analyticsCollectionService: AnalyticsCollectionService
    @Autowired
    lateinit var emailSender: MailSender
    @Autowired
    lateinit var config: EmailConfig

    @Scheduled(cron = "59 59 23 * * ?") // 11:59:59 PM every day
    fun checkOutstandingLogins() {
        val start = System.currentTimeMillis()
        logger.debug { "Processing outstanding logins" }

        try {
            emailSender.send(*users.findAllByInNow(true)
                    .map {
                        it to it.clockEvents.maxBy { e -> e.timestamp }
                    }
                    .map { (user, lastEvent) ->
                        // delete that last entry from the database
                        logger.debug { "User $user failed to logout at time ${lastEvent?.timestamp} (${lastEvent?.timestamp?.convertToISODate()})" }
                        users.save(user.copy(
                                clockEvents = user.clockEvents - lastEvent!!,
                                inNow = false,
                                lastEvent = (user.clockEvents - lastEvent).sortedBy { it.timestamp }.lastOrNull()?.timestamp
                        ))

                        analyticsCollectionService.recordEvent(start, "", "outstanding_login")

                        SimpleMailMessage().apply {
                            setFrom(config.emailFrom)
                            setReplyTo(config.emailReplyTo)
                            setTo(user.email)
                            setSubject("Lab Hours")
                            setText("You didn't sign out of the lab today; these hours have been lost.")
                        }
                    }.toTypedArray())
        } catch (e: MailException) {
            logger.error(e) { "Failed to send emails" }
        }

        logger.info { "Oustanding logins processed in ${System.currentTimeMillis() - start} ms" }
    }

}
