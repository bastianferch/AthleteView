package ase.athlete_view.domain.user.pojo.entity

import jakarta.persistence.*

@Entity
@Table(name = "users")
class User (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,

    @Column(unique = true)
    var email: String,

    var name: String,
    var password: String,
)
