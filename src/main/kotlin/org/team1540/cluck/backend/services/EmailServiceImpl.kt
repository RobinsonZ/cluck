package org.team1540.cluck.backend.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Conditional
import org.springframework.mail.MailException
import org.springframework.mail.MailSender
import org.springframework.mail.SimpleMailMessage
import org.springframework.stereotype.Service
import org.team1540.cluck.backend.data.SimpleEmail
import org.team1540.cluck.backend.interfaces.EmailService
import org.team1540.cluck.backend.testconditional.OfflineConditional

@Service
@Conditional(OfflineConditional::class)
class EmailServiceImpl : EmailService {

    @Autowired
    lateinit var emailSender: MailSender

    @Throws(MailException::class)
    override fun send(vararg emails: SimpleEmail) {
        val messages = mutableListOf<SimpleMailMessage>()
        for (email in emails) {
            val message = SimpleMailMessage()
            message.setTo(email.to)
            message.setSubject(email.subject)
            message.setText(email.text)
            messages += message
        }
        emailSender.send(*messages.toTypedArray())
    }
}

