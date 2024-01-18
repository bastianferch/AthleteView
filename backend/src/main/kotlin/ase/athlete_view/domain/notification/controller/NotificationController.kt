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

    private val logger = KotlinLogging.logger {}

    @GetMapping("/subscribe")
    fun subscribeToNotifications(@AuthenticationPrincipal user: UserDTO): SseEmitter? {
        logger.info { "GET subscribe to notifications by user with id ${user.id}" }
        val userId = user.id
        if (userId != null) {
            return notificationService.createEmitter(userId)
        }
        return null
    }

    @GetMapping
    fun getAllNotifications(@AuthenticationPrincipal user: UserDTO): List<NotificationDTO> {
        logger.info { "GET all notifications by user with id ${user.id}" }
        if (user.id == null) return listOf()
        return notificationService.getAllNotifications(user.id!!)
    }

    @DeleteMapping("/{id}")
    fun deleteNotification(@AuthenticationPrincipal user: UserDTO, @PathVariable(value="id") id: Long) {
        logger.info { "DELETE notification $id by user with id ${user.id}" }
        if (user.id != null) {
            notificationService.deleteNotification(user.id!!, id)
        }
    }

    @DeleteMapping
    fun deleteAllNotifications(@AuthenticationPrincipal user: UserDTO) {
        logger.info { "DELETE all notifications by user with id ${user.id}" }
        if (user.id != null) {
            notificationService.deleteAllNotifications(user.id!!)
        }
    }

    @PatchMapping
    fun markAllNotificationsAsRead(@AuthenticationPrincipal user: UserDTO) {
        logger.info { "PATCH mark all notifications as read by user with id ${user.id}" }
        if (user.id != null) {
            notificationService.markAllNotificationsAsRead(user.id!!)
        }
    }
}
