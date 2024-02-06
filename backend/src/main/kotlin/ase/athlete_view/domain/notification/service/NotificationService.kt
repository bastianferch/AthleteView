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
package ase.athlete_view.domain.notification.service

import ase.athlete_view.domain.notification.pojo.dto.NotificationDTO
import ase.athlete_view.domain.notification.pojo.entity.Notification
import ase.athlete_view.domain.notification.pojo.entity.NotificationType
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
    fun createEmitter(userId: Long): SseEmitter?

    /**
     * Sends notifications to a user, either per push notification or per mail.
     * Users receive push notifications in-app if they are online and want to receive push notifications of this type
     * Users receive emails in-app if they want to receive email notifications of this type.
     * If users opted out of emails when they are online, no emails are sent.
     *
     * @param userId of the user the notification should be sent to.
     * @param header of the notification. Should be <= 255 characters.
     * @param body (optional) text of the notification. Should be <= 255 characters.
     * @param link (optional) where the notification should lead the user. Should be <= 255 characters. Should be of the form "path/to/page". Examples: "activity", "activity/42". Don't use leading "/", don't specify the whole URL.
     * @param type (optional) type of notification. Used to check if user wants to receive notifications of this type.
     * @throws ValidationException if lengths of fields are greater than 255 chars.
     * @return the stored notification
     */
    fun sendNotification(userId: Long, header: String, body: String? = null, link: String? = null, type: NotificationType = NotificationType.DEFAULT): Notification?

    /**
     * Fetches all notifications of a user.
     *
     * @param userId of the user for whom the notifications should be fetched.
     * @return A List of NotificationDTO objects or an empty list if the user does not exist.
     */
    fun getAllNotifications(userId: Long): List<NotificationDTO>

    /**
     * Deletes a specific notification.
     *
     * @param userId of the user for which the notification should be deleted.
     * @param notificationId of the notification that should be deleted
     * @throws NotFoundException if the user exists but does not have a notification with the specified id
     */
    fun deleteNotification(userId: Long, notificationId: Long)

    /**
     * Deletes all Notifications of a user.
     * If the user does not exist, nothing happens.
     *
     * @param userId of the user for whom all Notifications should be deleted.
     */
    fun deleteAllNotifications(userId: Long)

    /**
     * Sets the `read` attribute of all Notifications of a user to true.
     *
     * @param userId of the user for whom all Notifications should be marked as read.
     */
    fun markAllNotificationsAsRead(userId: Long)
}
