package ase.athlete_view.unit

import ase.athlete_view.domain.mail.pojo.entity.Email
import ase.athlete_view.domain.mail.service.MailService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class MailServiceUnitTests {
    // if you want to check the email sending per se, remove the next two lines.
    @MockkBean
    private lateinit var javaMailSender: JavaMailSender //? = null

    @Autowired
    private val mailService: MailService? = null

    @Test
    @DisplayName("Email is sent")
    fun sendSimpleMailShouldSendCorrectMappedMail() {
        every { javaMailSender.send(any<SimpleMailMessage>()) } returns Unit

        val email = Email()
        email.recipient = "test@test.com"
        email.body = "Email body"
        email.subject = "Test subject"

        Assertions.assertDoesNotThrow{
            mailService?.sendSimpleMail(email)
        }
    }
}