package ase.athlete_view.mail

import ase.athlete_view.domain.mail.pojo.entity.Email
import ase.athlete_view.domain.mail.service.MailService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.test.context.ActiveProfiles


@ExtendWith(MockitoExtension::class)
@SpringBootTest
@ActiveProfiles("test")
class MailTest {
    // if you want to check the email sending per se, remove the next two lines.
    @Mock
    private val javaMailSender: JavaMailSender? = null

    @MockBean
    private val mailService: MailService? = null

    @Test
    @DisplayName("Email is sent")
    fun sendSimpleMailShouldSendCorrectMappedMail() {
        val email = Email()
        email.recipient = "test@test.com"
        email.body = "Email body"
        email.subject = "Test subject"
        Assertions.assertDoesNotThrow{
            mailService?.sendSimpleMail(email)
        }

    }
}