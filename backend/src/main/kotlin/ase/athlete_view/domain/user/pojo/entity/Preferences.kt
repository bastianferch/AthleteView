package ase.athlete_view.domain.user.pojo.entity

import ase.athlete_view.domain.user.pojo.dto.PreferencesDTO
import jakarta.persistence.*

@Entity
@Table(name = "preferences")
open class Preferences(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long?,
    // controls if users who are online receive email notifications even if they already received a push notification
    open var emailNotifications: Boolean = false,
    open var commentNotifications: NotificationPreferenceType = NotificationPreferenceType.PUSH,
    open var ratingNotifications: NotificationPreferenceType = NotificationPreferenceType.PUSH,
    open var otherNotifications: NotificationPreferenceType = NotificationPreferenceType.PUSH,

    open var shareHealthWithTrainer: Boolean = false
) {
    open fun toDTO(): PreferencesDTO {
        return PreferencesDTO(emailNotifications, commentNotifications, ratingNotifications, otherNotifications, shareHealthWithTrainer)
    }

    override fun toString(): String {
        return "Preferences(id=$id, emailNotifications=$emailNotifications, commentNotifications=$commentNotifications, ratingNotifications=$ratingNotifications, otherNotifications=$otherNotifications, shareHealthWithTrainer=$shareHealthWithTrainer)"
    }


}

enum class NotificationPreferenceType {
    EMAIL,
    PUSH,
    NONE,
    BOTH
}