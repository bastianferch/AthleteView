package ase.athlete_view.domain.user.pojo.dto

import ase.athlete_view.domain.user.pojo.entity.Preferences

class PreferencesDTO (
    var emailNotifications: Boolean,
) {
    fun toEntity(id: Long?): Preferences {
        return Preferences(id, emailNotifications)
    }

    override fun toString(): String {
        return "PreferencesDTO(emailNotifications=$emailNotifications)"
    }

}
