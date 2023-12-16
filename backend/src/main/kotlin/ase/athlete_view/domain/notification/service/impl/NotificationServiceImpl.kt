package ase.athlete_view.domain.notification.service.impl

import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.common.exception.entity.ValidationException
import ase.athlete_view.domain.mail.pojo.entity.Email
import ase.athlete_view.domain.mail.service.MailService
import ase.athlete_view.domain.notification.persistence.EmitterRepository
import ase.athlete_view.domain.notification.persistence.NotificationRepository
import ase.athlete_view.domain.notification.pojo.dto.NotificationDTO
import ase.athlete_view.domain.notification.pojo.entity.Notification
import ase.athlete_view.domain.notification.service.NotificationService
import ase.athlete_view.domain.user.persistence.UserRepository
import ase.athlete_view.domain.user.pojo.entity.User
import ase.athlete_view.domain.user.service.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.owasp.html.PolicyFactory
import org.owasp.html.Sanitizers
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
    ) : NotificationService {

    private val logger = KotlinLogging.logger {}

    override fun createEmitter(userId: Long): SseEmitter? {
        logger.trace { "NotificationServiceImpl.createEmitter($userId)" }

        // check if user exists
        val user = userRepository.findById(userId)
        if (!user.isPresent) {
            return null;
        }

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
        emitter.onError { e: Throwable? ->
            // this means we tried to send a notification over an already closed connection.
            // in this case, we delete the emitter since the connection is closed anyway.
            emitterRepository.deleteById(userId)
        }
        // replaces already existing emitters
        emitterRepository.save(userId, emitter);
        return emitter
    }

    /** Use this to send notifications to users.

     */
    override fun sendNotification(userId: Long, header: String, body: String?, link: String?): Notification? {
        logger.trace { "NotificationServiceImpl.sendNotification($userId, $header, $body, $link)" }

        val user = userRepository.findById(userId)
        if (!user.isPresent) {
            return null;
        }

        val userObj = user.get();

        val tempNotification = sanitizeNotification(Notification(null, userObj, false, Timestamp(System.currentTimeMillis()), header, body, link));
        validateNotification(tempNotification);
        val notification = notificationRepository.saveAndFlush(tempNotification);

        val preferences = user.get().preferences;

        // get the emitter for the user
        if (emitterRepository.existsById(userId)) {
            // this means the user is currently online, because we already have an emitter for this id.
            // we can send notifications directly to the user. No need for emails.
            val emitterOptional = emitterRepository.findById(userId)
            if (emitterOptional.isPresent) {
                val emitter = emitterOptional.get()
                try {
                    emitter.send(notification.toSseEventBuilder())
                } catch (e: IOException) {
                    // this happens when the connection is closed by the client, but the emitter is still stored.
                    // in this case, send an email instead (if email notifications are activated).
                    if (preferences != null && preferences.emailNotifications) {
                        sendNotificationEmail(userObj, notification);
                    }
                }
            }
        } else {
            // if the user is not online, send an email (if email notifications are activated).
            if (preferences != null && preferences.emailNotifications) {
                sendNotificationEmail(userObj, notification);
            }
        }
        return notification
    }

    private fun validateNotification(notification: Notification) {
        logger.trace { "NotificationServiceImpl.validateNotification($notification)" }
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

    //remove html tags except for styling such as <b> or <i>.
    private fun sanitizeNotification(notification: Notification): Notification {
        logger.trace { "NotificationServiceImpl.sanitizeNotification($notification)" }
        // only allow formatting tags in header and body
        val policy: PolicyFactory = Sanitizers.FORMATTING;
        return Notification(
            notification.id,
            notification.recipient,
            notification.read,
            notification.timestamp,
            policy.sanitize(notification.header),
            policy.sanitize(notification.body),
            notification.link);
    }

    private fun sendNotificationEmail(user: User, notification: Notification) {
        logger.trace { "NotificationServiceImpl.sendNotificationEmail($user, $notification)" }
        val subject = "AthleteView: New Notification"
        //TODO make link env variable
        val mailBody = "Hi ${user.name}!\nYou have a new notification:\n\n${notification.header}\n${notification.body}\n\nClick the following link to log in: http://localhost:4200/"
        val notificationMail = Email(recipient = user.email, body = mailBody, subject = subject);
        mailService.sendSimpleMail(notificationMail)
    }

    override fun getAllNotifications(userId: Long): List<NotificationDTO> {
        logger.trace { "NotificationServiceImpl.getAllNotifications($userId)" }
        val user = userRepository.findById(userId)
        if (!user.isPresent) {
            return listOf();
        }
        return user.get().notifications.map { it.toDTO() }
    }

    override fun deleteNotification(userId: Long, notificationId: Long) {
        logger.trace { "NotificationServiceImpl.deleteNotification($userId, $notificationId)" }
        val user = userRepository.findById(userId)
        if (user.isPresent) {
            // check if this notification really belongs to this user
            val notifications = user.get().notifications.filter { it.id == notificationId };
            if (notifications.isNotEmpty()) {
                notificationRepository.deleteById(notificationId);
            } else {
                throw NotFoundException("Notification not found")
            }
        }
    }

    override fun deleteAllNotifications(userId: Long) {
        logger.trace { "NotificationServiceImpl.deleteAllNotifications($userId)" }
        val user = userRepository.findById(userId)
        if (user.isPresent) {
            // check if this notification really belongs to this user
            val notifications = user.get().notifications;
            if (notifications.isNotEmpty()) {
                notificationRepository.deleteAllById(notifications.map { it.id })
            }
        }
    }

    override fun markAllNotificationsAsRead(userId: Long) {
        logger.trace { "NotificationServiceImpl.markAllNotificationsAsRead($userId)" }
        val user = userRepository.findById(userId)
        if (user.isPresent) {
            val notifications: List<Notification> = user.get().notifications.map {
                Notification(it.id, it.recipient, true, it.timestamp, it.header, it.body, it.link)
            };
            if (notifications.isNotEmpty()) {
                notificationRepository.saveAllAndFlush(notifications);
            }
        }
    }
}