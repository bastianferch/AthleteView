package ase.athlete_view.domain.mail.service.impl

import ase.athlete_view.common.exception.entity.ValidationException
import ase.athlete_view.config.mail.MailProperties
import ase.athlete_view.domain.mail.pojo.entity.Email
import ase.athlete_view.domain.mail.service.MailService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.mail.MailSendException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class MailServiceImpl(
    private val javaMailSender: JavaMailSender,
    private val mailProperties: MailProperties
) : MailService {

    val log = KotlinLogging.logger {}
    override fun sendSimpleMail(email: Email) {
        log.trace { "S | sendSimpleMail($email)" }
        val mailMessage = SimpleMailMessage()

        mailMessage.from = mailProperties.username
        mailMessage.setTo(email.recipient)
        mailMessage.text = email.body
        mailMessage.subject = email.subject

        try {
            javaMailSender.send(mailMessage)
        } catch (e: MailSendException) {
            throw ValidationException("Unreachable email address")
        }
    }
}