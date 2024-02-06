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
package ase.athlete_view.domain.notification.controller

import ase.athlete_view.domain.notification.pojo.dto.NotificationDTO
import ase.athlete_view.domain.notification.service.NotificationService
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RequestMapping("api/notification")
@RestController
class NotificationController(private val notificationService: NotificationService) {

    private val log = KotlinLogging.logger {}

    @GetMapping("/subscribe")
    fun subscribeToNotifications(@AuthenticationPrincipal user: UserDTO): SseEmitter? {
        log.info { "GET | subscribeToNotifications()" }
        val userId = user.id
        if (userId != null) {
            return notificationService.createEmitter(userId)
        }
        return null
    }

    @GetMapping
    fun getAllNotifications(@AuthenticationPrincipal user: UserDTO): List<NotificationDTO> {
        log.info { "GET | getAllNotifications()" }
        if (user.id == null) return listOf()
        return notificationService.getAllNotifications(user.id!!)
    }

    @DeleteMapping("/{id}")
    fun deleteNotification(@AuthenticationPrincipal user: UserDTO, @PathVariable(value="id") id: Long) {
        log.info { "DELETE | deleteNotification($id)" }
        if (user.id != null) {
            notificationService.deleteNotification(user.id!!, id)
        }
    }

    @DeleteMapping
    fun deleteAllNotifications(@AuthenticationPrincipal user: UserDTO) {
        log.info { "DELETE | deleteAllNotifications()" }
        if (user.id != null) {
            notificationService.deleteAllNotifications(user.id!!)
        }
    }

    @PatchMapping
    fun markAllNotificationsAsRead(@AuthenticationPrincipal user: UserDTO) {
        log.info { "PATCH | markAllNotificationsAsRead()" }
        if (user.id != null) {
            notificationService.markAllNotificationsAsRead(user.id!!)
        }
    }
}
