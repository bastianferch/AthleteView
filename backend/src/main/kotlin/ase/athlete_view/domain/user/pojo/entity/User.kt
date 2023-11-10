package ase.athlete_view.domain.user.pojo.entity

import jakarta.persistence.*

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "users")
abstract class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,

    @Column(unique = true)
    var email: String,

    var name: String,
    var password: String,
    var country: String,
    var zip: String,
)
