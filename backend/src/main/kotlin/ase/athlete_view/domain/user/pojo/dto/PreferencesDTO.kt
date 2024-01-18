package ase.athlete_view.domain.user.pojo.dto

import ase.athlete_view.domain.user.pojo.entity.NotificationPreferenceType
import ase.athlete_view.domain.user.pojo.entity.Preferences

class PreferencesDTO (
    var emailNotifications: Boolean,
    var commentNotifications: NotificationPreferenceType,
    var ratingNotifications: NotificationPreferenceType,
    var otherNotifications: NotificationPreferenceType,
    var shareHealthWithTrainer: Boolean

    ) {
    fun toEntity(id: Long?): Preferences {
        return Preferences(id, emailNotifications, commentNotifications, ratingNotifications, otherNotifications, shareHealthWithTrainer)
    }

    override fun toString(): String {
        return "PreferencesDTO(emailNotifications=$emailNotifications, commentNotifications=$commentNotifications, ratingNotifications=$ratingNotifications, otherNotifications=$otherNotifications, shareHealthWithTrainer=$shareHealthWithTrainer)"
    }

}
