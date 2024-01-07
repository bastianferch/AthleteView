package ase.athlete_view.domain.activity.pojo.entity

import ase.athlete_view.domain.activity.pojo.dto.IntervalDTO
import jakarta.persistence.*

@Entity
@Table(name = "ActivityInterval")
class Interval(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,

    var repeat: Int,

    @OneToMany(fetch = FetchType.EAGER)
    var intervals: List<Interval>?,

    @OneToOne(fetch = FetchType.EAGER)
    var step: Step?
) {
    fun toDTO(): IntervalDTO {
        return IntervalDTO(id, repeat, intervals?.map { it.toDTO() }, step?.toDTO())
    }

    fun copy(): Interval {
        val temp: MutableList<Interval> = mutableListOf()
        for (elem in this.intervals.orEmpty()){
            temp.add(elem.copy())
        }
        return Interval(
                null,
                this.repeat,
                temp,
                this.step?.copy()
        )
    }

    fun <E> List<E>.toDTOList(): List<IntervalDTO>? {
        return intervals?.map { it.toDTO() }
    }

    override fun toString(): String {
        return "Interval(id=$id, repeat=$repeat, intervals=$intervals, step=$step)"
    }
}

