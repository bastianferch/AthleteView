package ase.athlete_view.domain.user.pojo.entity

import ase.athlete_view.domain.notification.pojo.entity.Notification
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

    @OneToMany(mappedBy = "recipient", fetch = FetchType.LAZY, targetEntity = Notification::class)
    open var notifications: List<Notification> = mutableListOf(),

    open var name: String,
    open var password: String,
    open var country: String?,
    open var zip: String?,
    var isConfirmed: Boolean = false,

    @OneToMany(mappedBy = "createdBy", cascade = [CascadeType.MERGE, CascadeType.PERSIST], fetch = FetchType.LAZY)
    open var activities: MutableSet<PlannedActivity> = mutableSetOf(),

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "preferences_id", referencedColumnName = "id")
    open var preferences: Preferences? = null
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

    override fun toString(): String {
        return "User(id=$id, email='$email', name='$name', password='$password', country=$country, zip=$zip, isConfirmed=$isConfirmed, activities=$activities, notifications=$notifications)"
    }
}
