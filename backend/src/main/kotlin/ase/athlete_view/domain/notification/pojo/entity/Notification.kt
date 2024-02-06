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

enum class NotificationType {
    COMMENT,
    RATING,
    DEFAULT
}
