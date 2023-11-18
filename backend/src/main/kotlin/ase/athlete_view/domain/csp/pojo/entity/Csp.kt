package ase.athlete_view.domain.csp.pojo.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Csp(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?
)
