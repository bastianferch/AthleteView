package ase.athlete_view.domain.user.pojo.entity

import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import jakarta.persistence.*

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "users")
open class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long?,

    @Column(unique = true)
    open var email: String,

    open var name: String,
    open var password: String,
    open var country: String?,
    open var zip: String?,
    var isConfirmed: Boolean = false,

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "createdBy")
    open var activities: List<PlannedActivity> = listOf()
) {
    open fun toUserDTO(): UserDTO {
        return UserDTO(
            id,
            name,
            email,
            null,
            null,
            this.getUserType())
    }

    open fun getUserType(): String {
        return "undefined"
    }
}
