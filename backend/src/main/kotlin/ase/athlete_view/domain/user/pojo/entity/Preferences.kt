package ase.athlete_view.domain.user.pojo.entity

import ase.athlete_view.domain.user.pojo.dto.PreferencesDTO
import jakarta.persistence.*

@Entity
@Table(name = "preferences")
open class Preferences(
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    open var id: Long?,
    open var emailNotifications: Boolean = false,
) {
    open fun toDTO(): PreferencesDTO {
        return PreferencesDTO(emailNotifications);
    }
}
