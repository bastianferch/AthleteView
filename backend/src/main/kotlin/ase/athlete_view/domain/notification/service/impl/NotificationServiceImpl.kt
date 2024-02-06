package ase.athlete_view.domain.notification.service.impl

import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.common.exception.entity.ValidationException
import ase.athlete_view.common.sanitization.Sanitizer
import ase.athlete_view.domain.mail.pojo.entity.Email
import ase.athlete_view.domain.mail.service.MailService
import ase.athlete_view.domain.notification.persistence.EmitterRepository
import ase.athlete_view.domain.notification.persistence.NotificationRepository
import ase.athlete_view.domain.notification.pojo.dto.NotificationDTO
import ase.athlete_view.domain.notification.pojo.entity.Notification
import ase.athlete_view.domain.notification.pojo.entity.NotificationType
import ase.athlete_view.domain.notification.service.NotificationService
import ase.athlete_view.domain.user.persistence.UserRepository
import ase.athlete_view.domain.user.pojo.entity.NotificationPreferenceType
import ase.athlete_view.domain.user.pojo.entity.Preferences
import ase.athlete_view.domain.user.pojo.entity.User
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.sql.Timestamp


@Service
class NotificationServiceImpl(
    private val emitterRepository: EmitterRepository,
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository,
    private val mailService: MailService,
    private val sanitizer: Sanitizer,
    ) : NotificationService {

    private val log = KotlinLogging.logger {}

    override fun createEmitter(userId: Long): SseEmitter? {
        log.trace { " S | createEmitter($userId)" }

        // this is a session timeout, so it's not affected by activity on the channel
        // We set it to 3h. If we set an infinite timeout, there will be more and more useless emitters stored.
        // Setting 3h means that users who have the app open longer than 3h without logging out receive no push notifications, but emails,
        //  which is better than storing many unused emitters
        val emitter = SseEmitter(10800000L)
        emitter.onCompletion {
            emitterRepository.deleteById(userId)
        }
        emitter.onTimeout {
            emitterRepository.deleteById(userId)
        }
        emitter.onError { _: Throwable? ->
            // this means we tried to send a notification over an already closed connection.
            // in this case, we delete the emitter since the connection is closed anyway.
            emitterRepository.deleteById(userId)
        }
        // replaces already existing emitters
        emitterRepository.save(userId, emitter)
        return emitter
    }

    override fun sendNotification(userId: Long, header: String, body: String?, link: String?, type: NotificationType): Notification? {
        log.trace { "S | sendNotification($userId, $header, $body, $link, $type)" }

        val user = userRepository.findById(userId)
        if (!user.isPresent) {
            return null
        }

        val userObj = user.get()
        val preferences = user.get().preferences

        var notification = sanitizer.sanitizeNotification(Notification(null, userObj, false, Timestamp(System.currentTimeMillis()), header, body, link));

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

        validateNotification(notification);

        // if user does not want this push notification or per mail, return
        if (!canSendEmail(preferences, type) && !canSendPush(preferences, type)) {
            log.debug { "cant send notification as push nor per mail" }
            return null
        }

        // tracks if the notification was delivered in some way or stored to be delivered when user logs in next time
        var deliveredOrStored = false

        // tracks if a push notification was sent.
        // important because users can opt out of emails if push notification was already delivered.
        // This prevents emails when users are online anyway
        var pushDelivered = false


        // if the user wants to receive push notifications of this type
        if (canSendPush(preferences, type)) {
            // only save the notification if the user wants to receive push notifications
            // otherwise they would get it the next time they log in / refresh the page
            notification = notificationRepository.saveAndFlush(notification)
            deliveredOrStored = true
            // get the emitter for the user
            if (emitterRepository.existsById(userId)) {
                // this means the user is currently online, because we already have an emitter for this id.
                // we can send notifications directly to the user. No need for emails.
                val emitterOptional = emitterRepository.findById(userId)
                if (emitterOptional.isPresent) {
                    val emitter = emitterOptional.get()
                    try {
                        emitter.send(notification.toSseEventBuilder())
                        pushDelivered = true
                    } catch (e: IOException) {
                        // this happens when the connection is closed by the client, but the emitter is still stored.
                        // close the emitter and delete it
                        emitterRepository.deleteById(userId)
                    }
                }
            }
        }

        // if the user wants email notifications about this type of notification
        if (canSendEmail(preferences, type)) {
            // if the push notification was already delivered and users opted out of emails in this case, send nothing
            if (!(pushDelivered && preferences != null && !preferences.emailNotifications)) {
                sendNotificationEmail(userObj, notification)
                deliveredOrStored = true
            }
        }

        // only return the notification object if something was delivered
        return if (deliveredOrStored) {
            notification
        } else {
            null
        }
    }

    private fun validateNotification(notification: Notification) {
        log.trace { "S | validateNotification($notification)" }
        if (notification.header.length > 255) {
            throw ValidationException("Notification Header must be <= 255 characters")
        }

        if (notification.body != null && notification.body!!.length > 255) {
            throw ValidationException("Notification Body must be <= 255 characters")
        }

        if (notification.link != null && notification.link!!.length > 255) {
            throw ValidationException("Notification Link must be <= 255 characters")
        }
    }

    private fun sendNotificationEmail(user: User, notification: Notification) {
        log.trace { "S | sendNotificationEmail($user, $notification)" }
        val subject = "AthleteView: New Notification"
        //TODO make link env variable
        val mailBody = "Hi ${user.name}!\nYou have a new notification:\n\n${notification.header}\n${notification.body}\n\nClick the following link to log in: http://localhost:4200/"
        val notificationMail = Email(recipient = user.email, body = mailBody, subject = subject)
        mailService.sendSimpleMail(notificationMail)
    }

    private fun canSendEmail(preferences: Preferences?, notificationType: NotificationType): Boolean {
        log.trace { "S | canSendEmail($preferences, $notificationType)" }
        if (preferences == null) {
            return false
        }

        if (notificationType == NotificationType.DEFAULT) {
            if (preferences.otherNotifications == NotificationPreferenceType.NONE
                || preferences.otherNotifications == NotificationPreferenceType.PUSH) {
                return false
            }
        }

        if (notificationType == NotificationType.COMMENT) {
            if (preferences.commentNotifications == NotificationPreferenceType.NONE
                || preferences.commentNotifications == NotificationPreferenceType.PUSH) {
                return false
            }
        }

        if (notificationType == NotificationType.RATING) {
            if (preferences.ratingNotifications == NotificationPreferenceType.NONE
                || preferences.ratingNotifications == NotificationPreferenceType.PUSH) {
                return false
            }
        }

        return true
    }

    private fun canSendPush(preferences: Preferences?, notificationType: NotificationType): Boolean {
        log.trace { "S | canSendPush($preferences, $notificationType)" }
        if (preferences == null) {
            return false
        }

        if (notificationType == NotificationType.DEFAULT) {
            if (preferences.otherNotifications == NotificationPreferenceType.NONE
                || preferences.otherNotifications == NotificationPreferenceType.EMAIL) {
                return false
            }
        }

        if (notificationType == NotificationType.COMMENT) {
            if (preferences.commentNotifications == NotificationPreferenceType.NONE
                || preferences.commentNotifications == NotificationPreferenceType.EMAIL) {
                return false
            }
        }

        if (notificationType == NotificationType.RATING) {
            if (preferences.ratingNotifications == NotificationPreferenceType.NONE
                || preferences.ratingNotifications == NotificationPreferenceType.EMAIL) {
                return false
            }
        }

        return true
    }

    override fun getAllNotifications(userId: Long): List<NotificationDTO> {
        log.trace { "S | getAllNotifications($userId)" }
        val user = userRepository.findById(userId)
        if (!user.isPresent) {
            return listOf()
        }
        return user.get().notifications.map { it.toDTO() }
    }

    override fun deleteNotification(userId: Long, notificationId: Long) {
        log.trace { "S | deleteNotification($userId, $notificationId)" }
        val user = userRepository.findById(userId)
        if (user.isPresent) {
            // check if this notification really belongs to this user
            val notifications = user.get().notifications.filter { it.id == notificationId }
            if (notifications.isNotEmpty()) {
                notificationRepository.deleteById(notificationId)
            } else {
                throw NotFoundException("Notification not found")
            }
        }
    }

    override fun deleteAllNotifications(userId: Long) {
        log.trace { "S | deleteAllNotifications($userId)" }
        val user = userRepository.findById(userId)
        if (user.isPresent) {
            // check if this notification really belongs to this user
            val notifications = user.get().notifications
            if (notifications.isNotEmpty()) {
                notificationRepository.deleteAllById(notifications.map { it.id })
            }
        }
    }

    override fun markAllNotificationsAsRead(userId: Long) {
        log.trace { "S | markAllNotificationsAsRead($userId)" }
        val user = userRepository.findById(userId)
        if (user.isPresent) {
            val notifications: List<Notification> = user.get().notifications.map {
                Notification(it.id, it.recipient, true, it.timestamp, it.header, it.body, it.link)
            }
            if (notifications.isNotEmpty()) {
                notificationRepository.saveAllAndFlush(notifications)
            }
        }
    }
}
