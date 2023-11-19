package ase.athlete_view.domain.activity.pojo.entity

import jakarta.persistence.*

@Entity
class Interval(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int?,

    val repeat: Int,

    @OneToMany
    val intervals: List<Interval>?,

    @OneToOne
    val step: Step?
)