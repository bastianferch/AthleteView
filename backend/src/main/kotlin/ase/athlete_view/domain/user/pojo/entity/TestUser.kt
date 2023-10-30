package ase.athlete_view.domain.user.pojo.entity

import jakarta.persistence.*
import lombok.*

@Entity
class TestUser (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var name: String? = null
)
