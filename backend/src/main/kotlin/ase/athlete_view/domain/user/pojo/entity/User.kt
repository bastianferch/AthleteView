package ase.athlete_view.domain.user.pojo.entity

import ase.athlete_view.domain.user.pojo.dto.UserDto
import jakarta.persistence.*

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "users")
abstract class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long?,

    @Column(unique = true)
    open var email: String,

    open var name: String,
    open var password: String,
    open var country: String,
    open var zip: String,
) {
    abstract fun toUserDto(): UserDto

}
