package ase.athlete_view.domain.user.pojo.entity

import ase.athlete_view.domain.user.pojo.dto.PreferencesDTO
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "preferences")
open class Preferences(
    @Id
    open var id: Long?,
    open var emailNotifications: Boolean = false,
) {
    open fun toDTO(): PreferencesDTO {
        return PreferencesDTO(emailNotifications);
    }
}