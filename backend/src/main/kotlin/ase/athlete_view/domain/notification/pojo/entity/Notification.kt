package ase.athlete_view.domain.notification.pojo.entity

import ase.athlete_view.domain.notification.pojo.dto.NotificationDTO
import ase.athlete_view.domain.user.pojo.entity.User
import jakarta.persistence.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder
import java.sql.Timestamp


@Entity
@Table(name = "notification")
open class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    open var recipient: User,
    open var read: Boolean = true,
    open var timestamp: Timestamp,
    open var header: String,
    open var body: String?,
    open var link: String?
) {
    open fun toDTO(): NotificationDTO {
        return NotificationDTO(id, header, body, link, read, timestamp.time)
    }

    open fun toSseEventBuilder(): SseEventBuilder {
        return SseEmitter.event()
            .id(id.toString())
            .data(toDTO())
    }

    override fun toString(): String {
        return "Notification(id=$id, recipient=${recipient.id}, read=$read, timestamp=$timestamp, header='$header', body=$body, link=$link)"
    }


}