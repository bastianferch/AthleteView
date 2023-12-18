package ase.athlete_view.unit

import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.common.exception.entity.ValidationException
import ase.athlete_view.domain.mail.service.MailService
import ase.athlete_view.domain.notification.persistence.EmitterRepository
import ase.athlete_view.domain.notification.persistence.NotificationRepository
import ase.athlete_view.domain.notification.pojo.entity.Notification
import ase.athlete_view.domain.notification.service.NotificationService
import ase.athlete_view.domain.user.persistence.UserRepository
import ase.athlete_view.domain.user.pojo.entity.Preferences
import ase.athlete_view.util.UserCreator
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import jakarta.transaction.Transactional
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.sql.Timestamp
import java.util.*


@SpringBootTest
@ActiveProfiles("test")
class NotificationServiceUnitTests {

    @Autowired
    private lateinit var notificationService: NotificationService

    @MockkBean
    private lateinit var emitterRepository: EmitterRepository

    @MockkBean
    private lateinit var notificationRepository: NotificationRepository

    @MockkBean
    private lateinit var userRepository: UserRepository

    @MockkBean
    private lateinit var mailService: MailService


    private var user = UserCreator.getAthlete(null)
    private var trainer = UserCreator.getTrainer()
    private var preferences = Preferences(user.id)

    private val header = "header"
    private val body = "body"
    private val link = "link"

    private val notification = Notification(0, user, false, Timestamp(System.currentTimeMillis()), header, body, link)
    private val otherNotification = Notification(420, user,false, Timestamp(System.currentTimeMillis()), "","","");


    @BeforeEach
    fun beforeEach() {
        user.preferences = preferences
    }


    // createEmitter

    @Test
    @Transactional
    fun createEmitterForUser_shouldCallEmitterRepositorySaveAndReturnEmitter() {
        every { userRepository.findById(user.id!!) } returns Optional.of(user)
        every { emitterRepository.save(any<Long>(), any()) } returns Unit;
        val emitter = notificationService.createEmitter(user.id!!)

        assert(emitter != null)

        // verify emitter is saved
        verify(exactly = 1) { emitterRepository.save(any(), any()) }
    }


    // sendNotification

    @Test
    @Transactional
    fun sendTooLongNotification_shouldThrowValidationException() {
        every { userRepository.findById(any<Long>()) } returns Optional.of(UserCreator.getAthlete(null))

        val tooLong = "toooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooLong"
        val shortHeader = "shortHeader"
        val shortBody = "short Body"
        val shortLink = "shortLink"

        assertThrows<ValidationException> { notificationService.sendNotification(user.id!!, tooLong, tooLong, tooLong) }
        assertThrows<ValidationException> { notificationService.sendNotification(user.id!!, tooLong, shortBody, shortLink) }
        assertThrows<ValidationException> { notificationService.sendNotification(user.id!!, shortHeader, tooLong, shortLink) }
        assertThrows<ValidationException> { notificationService.sendNotification(user.id!!, shortHeader, shortBody, tooLong) }
    }

    @Test
    @Transactional
    fun sendValidNotificationForUserWithEmailPreferencesFalse_shouldSendNoMail() {
        every { userRepository.findById(any<Long>()) } returns Optional.of(user)
        // user is offline
        every { emitterRepository.existsById(any<Long>()) } returns false;
        every { mailService.sendSimpleMail(any()) } returns Unit
        every { notificationRepository.saveAndFlush(any()) } returns notification

        assertDoesNotThrow { notificationService.sendNotification(user.id!!, header, body, link) }

        // verify no email was sent
        verify(exactly = 0) { mailService.sendSimpleMail(any()) }
    }

    @Test
    @Transactional
    fun sendValidNotificationForUserWithEmailPreferencesTrue_shouldSendMail() {
        // user wants to be notified via mail
        user.preferences = Preferences(user.id!!, true);
        every { userRepository.findById(any<Long>()) } returns Optional.of(user)

        // user is offline
        every { emitterRepository.existsById(any<Long>()) } returns false;
        every { mailService.sendSimpleMail(any()) } returns Unit
        every { notificationRepository.saveAndFlush(any()) } returns notification

        assertDoesNotThrow { notificationService.sendNotification(user.id!!, header, body, link) }

        // verify email was sent
        verify(exactly = 1) { mailService.sendSimpleMail(any()) }
    }


    // getAllNotifications

    @Test
    @Transactional
    fun getAllNotificationsOfUsers_shouldReturnNotificationsPropertyOfObjects() {
        every { userRepository.findById(user.id!!) } returns Optional.of(user)
        every { userRepository.findById(trainer.id!!) } returns Optional.of(trainer)

        user.notifications = listOf(notification, notification)
        trainer.notifications = listOf(notification)

        val allNotificationsUser = notificationService.getAllNotifications(user.id!!)
        val allNotificationsTrainer = notificationService.getAllNotifications(trainer.id!!)

        assert(allNotificationsUser.size == 2)
        assert(allNotificationsTrainer.size == 1)
    }

    @Test
    @Transactional
    fun getAllNotificationsOfNonExistentUser_shouldReturnEmptyList() {
        every { userRepository.findById(user.id!!) } returns Optional.of(user)
        every { userRepository.findById(trainer.id!!) } returns Optional.of(trainer)

        // user 420 does not exist :(
        every { userRepository.findById(420) } returns Optional.empty()

        // give existing users a few notifications to check that function does not get all existing notifications
        user.notifications = listOf(notification, notification)
        trainer.notifications = listOf(notification)

        val allNotificationsNonexistent = notificationService.getAllNotifications(420)

        assert(allNotificationsNonexistent.isEmpty())
    }




    // deleteNotification
    @Test
    @Transactional
    fun deleteNotificationWithRightUser_shouldCallDeleteById() {
        every { userRepository.findById(user.id!!) } returns Optional.of(user)
        every { notificationRepository.deleteById(any()) } returns Unit

        // give existing user a notification
        user.notifications = listOf(notification)

        // delete notification
        this.notificationService.deleteNotification(user.id!!, notification.id!!)

        //the repository should have been called
        verify(exactly = 1) { notificationRepository.deleteById(notification.id!!) }
    }


    @Test
    @Transactional
    fun deleteNotificationWithWrongUser_shouldThrowNotFoundAndNotCallDeleteById() {
        every { userRepository.findById(user.id!!) } returns Optional.of(user)
        every { userRepository.findById(trainer.id!!) } returns Optional.of(trainer)
        every { notificationRepository.deleteById(any()) } returns Unit

        // give existing user a notification
        user.notifications = listOf(notification)
        trainer.notifications = listOf(otherNotification)

        // delete notification with wrong user
        assertThrows<NotFoundException> { this.notificationService.deleteNotification(user.id!!, otherNotification.id!!) }
        assertThrows<NotFoundException> { this.notificationService.deleteNotification(trainer.id!!, notification.id!!) }

        //the repository should not be called
        verify(exactly = 0) { notificationRepository.deleteById(notification.id!!) }
        verify(exactly = 0) { notificationRepository.deleteById(otherNotification.id!!) }
    }


    // deleteAllNotifications

    @Test
    @Transactional
    fun deleteAllNotificationsWithRightUser_shouldCallDeleteAllById() {
        every { userRepository.findById(user.id!!) } returns Optional.of(user)
        every { notificationRepository.deleteAllById(any()) } returns Unit

        // give existing user a notification
        user.notifications = listOf(notification)

        // delete notification
        this.notificationService.deleteAllNotifications(user.id!!)

        //the repository should have been called
        verify(exactly = 1) { notificationRepository.deleteAllById(listOf(notification.id!!)) }
    }


    @Test
    @Transactional
    fun deleteAllNotificationsWithNonexistentUser_shouldNotCallDeleteAllById() {
        every { userRepository.findById(user.id!!) } returns Optional.empty()
        every { notificationRepository.deleteAllById(any()) } returns Unit

        this.notificationService.deleteAllNotifications(user.id!!)

        //the repository should not be called
        verify(exactly = 0) { notificationRepository.deleteById(any()) }
    }

    @Test
    @Transactional
    fun deleteAllNotificationsWithUserWithoutNotifications_shouldNotCallDeleteAllById() {
        every { userRepository.findById(user.id!!) } returns Optional.of(user)
        every { notificationRepository.deleteAllById(any()) } returns Unit

        // user has nno notifications
        user.notifications = listOf()

        // delete notification
        this.notificationService.deleteAllNotifications(user.id!!)

        //the repository should have been called
        verify(exactly = 0) { notificationRepository.deleteAllById(any()) }
    }


    // markAllNotificationsAsRead

    @Test
    @Transactional
    fun markAllNotificationsAsReadWithRightUser_shouldCallSaveAllAndFlush() {
        every { userRepository.findById(user.id!!) } returns Optional.of(user)
        every { notificationRepository.saveAllAndFlush(any<List<Notification>>()) } returns listOf()

        // give existing user a notification
        user.notifications = listOf(notification)

        // TODO can we check that the arguments have read == true?
        this.notificationService.markAllNotificationsAsRead(user.id!!)
        verify(exactly = 1) { notificationRepository.saveAllAndFlush(any<List<Notification>>()) }
    }


    @Test
    @Transactional
    fun markAllNotificationsAsReadWithNonexistentUser_shouldNotCallSaveAllAndFlush() {
        every { userRepository.findById(user.id!!) } returns Optional.empty()
        every { notificationRepository.saveAllAndFlush(any<List<Notification>>()) } returns listOf()

        this.notificationService.markAllNotificationsAsRead(user.id!!)
        verify(exactly = 0) { notificationRepository.saveAllAndFlush(any<List<Notification>>()) }
    }

    @Test
    @Transactional
    fun markAllNotificationsAsReadWithUserWithNoNotifications_shouldNotCallSaveAllAndFlush() {
        every { userRepository.findById(user.id!!) } returns Optional.of(user)
        every { notificationRepository.saveAllAndFlush(any<List<Notification>>()) } returns listOf()

        user.notifications = listOf()

        this.notificationService.markAllNotificationsAsRead(user.id!!)
        verify(exactly = 0) { notificationRepository.saveAllAndFlush(any<List<Notification>>()) }
    }
}