/*-
 * #%L
 * athlete_view
 * %%
 * Copyright (C) 2023 - 2024 TU Wien INSO ASE GROUP 5 WS2023
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
package ase.athlete_view.unit

import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.common.exception.entity.ValidationException
import ase.athlete_view.domain.mail.service.MailService
import ase.athlete_view.domain.notification.persistence.EmitterRepository
import ase.athlete_view.domain.notification.persistence.NotificationRepository
import ase.athlete_view.domain.notification.pojo.entity.Notification
import ase.athlete_view.domain.notification.pojo.entity.NotificationType
import ase.athlete_view.domain.notification.service.NotificationService
import ase.athlete_view.domain.user.persistence.UserRepository
import ase.athlete_view.domain.user.pojo.entity.NotificationPreferenceType
import ase.athlete_view.domain.user.pojo.entity.Preferences
import ase.athlete_view.util.UserCreator
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import jakarta.transaction.Transactional
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
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
    private val otherNotification = Notification(420, user,false, Timestamp(System.currentTimeMillis()), "","","")


    @BeforeEach
    fun beforeEach() {
        // set preferences of test user
        user.preferences = preferences
    }

    fun getEmitter(): SseEmitter {
        val emitter = SseEmitter(600000)
        emitter.onCompletion {}
        emitter.onTimeout {}
        emitter.onError {}
        return emitter
    }


    // createEmitter

    @Test
    @Transactional
    fun createEmitterForUser_shouldCallEmitterRepositorySaveAndReturnEmitter() {
        every { userRepository.findById(user.id!!) } returns Optional.of(user)
        every { emitterRepository.save(any<Long>(), any()) } returns Unit
        val emitter = notificationService.createEmitter(user.id!!)

        assertNotNull(emitter)

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
    fun sendValidNotificationForOfflineUserWithNotificationPreferencesPushOnly_shouldSendNoMailAndStoreNotification() {

        // user only wants push notifications
        user.preferences = Preferences(
            id = user.id,
            emailNotifications = false,
            commentNotifications = NotificationPreferenceType.PUSH,
            ratingNotifications = NotificationPreferenceType.PUSH,
            otherNotifications = NotificationPreferenceType.PUSH
        )

        every { userRepository.findById(any<Long>()) } returns Optional.of(user)
        // user is offline
        every { emitterRepository.existsById(any<Long>()) } returns false
        every { mailService.sendSimpleMail(any()) } returns Unit
        every { notificationRepository.saveAndFlush(any()) } returns notification

        assertDoesNotThrow { notificationService.sendNotification(user.id!!, header, body, link, NotificationType.DEFAULT) }
        assertDoesNotThrow { notificationService.sendNotification(user.id!!, header, body, link, NotificationType.COMMENT) }
        assertDoesNotThrow { notificationService.sendNotification(user.id!!, header, body, link, NotificationType.RATING) }

        // verify no email was sent
        verify(exactly = 0) { mailService.sendSimpleMail(any()) }

        // verify that notifications were stored
        verify(exactly = 3) { notificationRepository.saveAndFlush(any()) }
    }

    @Test
    @Transactional
    fun sendValidNotificationForOfflineUserWithNotificationPreferencesEmailOnly_shouldSendMailsAndNotStoreNotifications() {

        user.preferences = Preferences(
            id = user.id,
            emailNotifications = false,
            commentNotifications = NotificationPreferenceType.EMAIL,
            ratingNotifications = NotificationPreferenceType.EMAIL,
            otherNotifications = NotificationPreferenceType.EMAIL
        )

        every { userRepository.findById(any<Long>()) } returns Optional.of(user)
        // user is offline
        every { emitterRepository.existsById(any<Long>()) } returns false
        every { mailService.sendSimpleMail(any()) } returns Unit
        every { notificationRepository.saveAndFlush(any()) } returns notification

        assertDoesNotThrow { notificationService.sendNotification(user.id!!, header, body, link, NotificationType.DEFAULT) }
        assertDoesNotThrow { notificationService.sendNotification(user.id!!, header, body, link, NotificationType.COMMENT) }
        assertDoesNotThrow { notificationService.sendNotification(user.id!!, header, body, link, NotificationType.RATING) }

        // verify emails were sent
        verify(exactly = 3) { mailService.sendSimpleMail(any()) }

        // verify that no notifications were stored
        verify(exactly = 0) { notificationRepository.saveAndFlush(any()) }
    }

    @Test
    @Transactional
    fun sendValidNotificationForOfflineUserWithNotificationPreferencesEmailAndPush_shouldSendMailsAndStoreNotifications() {

        user.preferences = Preferences(
            id = user.id,
            emailNotifications = false,
            commentNotifications = NotificationPreferenceType.BOTH,
            ratingNotifications = NotificationPreferenceType.BOTH,
            otherNotifications = NotificationPreferenceType.BOTH
        )

        val emitter = SseEmitter(600000)
        emitter.onCompletion {}
        emitter.onTimeout {}
        emitter.onError {}

        every { userRepository.findById(any<Long>()) } returns Optional.of(user)
        // user is offline
        every { emitterRepository.existsById(any<Long>()) } returns false
        every { mailService.sendSimpleMail(any()) } returns Unit
        every { notificationRepository.saveAndFlush(any()) } returns notification

        assertDoesNotThrow { notificationService.sendNotification(user.id!!, header, body, link, NotificationType.DEFAULT) }
        assertDoesNotThrow { notificationService.sendNotification(user.id!!, header, body, link, NotificationType.COMMENT) }
        assertDoesNotThrow { notificationService.sendNotification(user.id!!, header, body, link, NotificationType.RATING) }

        // verify emails were sent
        verify(exactly = 3) { mailService.sendSimpleMail(any()) }

        // verify that notifications were stored
        verify(exactly = 3) { notificationRepository.saveAndFlush(any()) }
    }

    @Test
    @Transactional
    fun sendValidNotificationForOnlineUserWithNotificationPreferencesEmailAndPushButNoMailsWhenOnline_shouldNotSendMailsAndStoreNotifications() {

        user.preferences = Preferences(
            id = user.id,
            emailNotifications = false,
            commentNotifications = NotificationPreferenceType.BOTH,
            ratingNotifications = NotificationPreferenceType.BOTH,
            otherNotifications = NotificationPreferenceType.BOTH
        )

        val emitter = getEmitter()

        every { userRepository.findById(any<Long>()) } returns Optional.of(user)
        // user is offline
        every { emitterRepository.existsById(any<Long>()) } returns true
        every { emitterRepository.findById(any<Long>()) } returns Optional.of(emitter)
        every { mailService.sendSimpleMail(any()) } returns Unit
        every { notificationRepository.saveAndFlush(any()) } returns notification

        assertDoesNotThrow { notificationService.sendNotification(user.id!!, header, body, link, NotificationType.DEFAULT) }
        assertDoesNotThrow { notificationService.sendNotification(user.id!!, header, body, link, NotificationType.COMMENT) }
        assertDoesNotThrow { notificationService.sendNotification(user.id!!, header, body, link, NotificationType.RATING) }

        // verify no emails were sent
        verify(exactly = 0) { mailService.sendSimpleMail(any()) }

        // verify that notifications were stored
        verify(exactly = 3) { notificationRepository.saveAndFlush(any()) }
    }

    @Test
    @Transactional
    fun sendValidNotificationForOnlineUserWithNotificationPreferencesEmailAndPush_shouldSendMailsAndStoreNotifications() {

        user.preferences = Preferences(
            id = user.id,
            emailNotifications = true,
            commentNotifications = NotificationPreferenceType.BOTH,
            ratingNotifications = NotificationPreferenceType.BOTH,
            otherNotifications = NotificationPreferenceType.BOTH
        )

        val emitter = getEmitter()

        every { userRepository.findById(any<Long>()) } returns Optional.of(user)
        // user is offline
        every { emitterRepository.existsById(any<Long>()) } returns true
        every { emitterRepository.findById(any<Long>()) } returns Optional.of(emitter)
        every { mailService.sendSimpleMail(any()) } returns Unit
        every { notificationRepository.saveAndFlush(any()) } returns notification

        assertDoesNotThrow { notificationService.sendNotification(user.id!!, header, body, link, NotificationType.DEFAULT) }
        assertDoesNotThrow { notificationService.sendNotification(user.id!!, header, body, link, NotificationType.COMMENT) }
        assertDoesNotThrow { notificationService.sendNotification(user.id!!, header, body, link, NotificationType.RATING) }

        // verify emails were sent
        verify(exactly = 3) { mailService.sendSimpleMail(any()) }

        // verify that notifications were stored
        verify(exactly = 3) { notificationRepository.saveAndFlush(any()) }
    }

    @Test
    @Transactional
    fun sendValidNotificationForOnlineUserWithNotificationPreferencesEmail_shouldSendMailsAndNotStoreNotifications() {

        user.preferences = Preferences(
            id = user.id,
            emailNotifications = true,
            commentNotifications = NotificationPreferenceType.EMAIL,
            ratingNotifications = NotificationPreferenceType.EMAIL,
            otherNotifications = NotificationPreferenceType.EMAIL
        )

        val emitter = getEmitter()

        every { userRepository.findById(any<Long>()) } returns Optional.of(user)
        // user is offline
        every { emitterRepository.existsById(any<Long>()) } returns true
        every { emitterRepository.findById(any<Long>()) } returns Optional.of(emitter)
        every { mailService.sendSimpleMail(any()) } returns Unit
        every { notificationRepository.saveAndFlush(any()) } returns notification

        assertDoesNotThrow { notificationService.sendNotification(user.id!!, header, body, link, NotificationType.DEFAULT) }
        assertDoesNotThrow { notificationService.sendNotification(user.id!!, header, body, link, NotificationType.COMMENT) }
        assertDoesNotThrow { notificationService.sendNotification(user.id!!, header, body, link, NotificationType.RATING) }

        // verify emails were sent
        verify(exactly = 3) { mailService.sendSimpleMail(any()) }

        // verify that no notifications were stored
        verify(exactly = 0) { notificationRepository.saveAndFlush(any()) }
    }

    @Test
    @Transactional
    fun sendValidNotificationForOnlineUserWithNotificationPreferencesNone_shouldNotSendMailsAndStoreNoNotifications() {

        user.preferences = Preferences(
            id = user.id,
            emailNotifications = true,
            commentNotifications = NotificationPreferenceType.NONE,
            ratingNotifications = NotificationPreferenceType.NONE,
            otherNotifications = NotificationPreferenceType.NONE
        )

        val emitter = getEmitter()

        every { userRepository.findById(any<Long>()) } returns Optional.of(user)
        // user is offline
        every { emitterRepository.existsById(any<Long>()) } returns true
        every { emitterRepository.findById(any<Long>()) } returns Optional.of(emitter)
        every { mailService.sendSimpleMail(any()) } returns Unit
        every { notificationRepository.saveAndFlush(any()) } returns notification

        assertDoesNotThrow { notificationService.sendNotification(user.id!!, header, body, link, NotificationType.DEFAULT) }
        assertDoesNotThrow { notificationService.sendNotification(user.id!!, header, body, link, NotificationType.COMMENT) }
        assertDoesNotThrow { notificationService.sendNotification(user.id!!, header, body, link, NotificationType.RATING) }

        // verify no emails were sent
        verify(exactly = 0) { mailService.sendSimpleMail(any()) }

        // verify that no notifications were stored
        verify(exactly = 0) { notificationRepository.saveAndFlush(any()) }
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

        assertEquals(allNotificationsUser.size, 2)
        assertEquals(allNotificationsTrainer.size, 1)
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

        assertTrue(allNotificationsNonexistent.isEmpty())
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
