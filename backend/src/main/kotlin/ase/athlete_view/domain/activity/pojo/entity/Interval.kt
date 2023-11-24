package ase.athlete_view.domain.activity.pojo.entity

import ase.athlete_view.domain.activity.pojo.dto.IntervalDTO
import jakarta.persistence.*

@Entity
class Interval(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,

    val repeat: Int,

    @OneToMany
    val intervals: List<Interval>?,

    @OneToOne
    val step: Step?
) {
    fun toDTO(): IntervalDTO {
        return IntervalDTO(id, repeat, intervals?.toDTOList(), step?.toDTO())
    }

    fun <E> List<E>.toDTOList(): List<IntervalDTO>? {
        return intervals?.map { it.toDTO() }
    }

    override fun toString(): String {
        return "Interval(id=$id, repeat=$repeat, intervals=$intervals, step=$step)"
    }
}

