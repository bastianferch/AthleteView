package ase.athlete_view.domain.notification.service

import ase.athlete_view.domain.notification.pojo.dto.NotificationDTO
import ase.athlete_view.domain.notification.pojo.entity.Notification
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

interface NotificationService {

    /**
     * If the user with the give userId exists, create and save an
     * SSEEmitter that is used to send push Notifications to the frontend.
     * If there already is an emitter, it is replaced by the new one.
     *
     * @param userId of the user for which the emitter should be created
     * @return the created emitter, or null if the user does not exist
     */
    fun createEmitter(userId: Long): SseEmitter?;

    /**
     * Sends notifications to a user, either per push notification or per mail.
     * If the user is online, they receive push notifications in-app
     * Otherwise they receive the notification per email, except if email notifications are disabled via the Preferences object.
     * The new notification is stored in the repository after being validated and sanitized (potentially harmful html is removed).
     *
     * @param userId of the user the notification should be sent to.
     * @param header of the notification. Should be <= 255 characters.
     * @param body (optional) text of the notification. Should be <= 255 characters.
     * @param link (optional) where the notification should lead the user. Should be <= 255 characters. Should be of the form "path/to/page". Examples: "activity", "activity/42". Don't use leading "/", don't specify the whole URL.
     * @throws ValidationException if lengths of fields are greater than 255 chars.
     * @return the stored notification
     */
    fun sendNotification(userId: Long, header: String, body: String? = null, link: String? = null): Notification?;

    /**
     * Fetches all notifications of a user.
     *
     * @param userId of the user for whom the notifications should be fetched.
     * @return A List of NotificationDTO objects or an empty list if the user does not exist.
     */
    fun getAllNotifications(userId: Long): List<NotificationDTO>;

    /**
     * Deletes a specific notification.
     *
     * @param userId of the user for which the notification should be deleted.
     * @param notificationId of the notification that should be deleted
     * @throws NotFoundException if the user exists but does not have a notification with the specified id
     */
    fun deleteNotification(userId: Long, notificationId: Long);

    /**
     * Deletes all Notifications of a user.
     * If the user does not exist, nothing happens.
     *
     * @param userId of the user for whom all Notifications should be deleted.
     */
    fun deleteAllNotifications(userId: Long);

    /**
     * Sets the `read` attribute of all Notifications of a user to true.
     *
     * @param userId of the user for whom all Notifications should be marked as read.
     */
    fun markAllNotificationsAsRead(userId: Long);
}