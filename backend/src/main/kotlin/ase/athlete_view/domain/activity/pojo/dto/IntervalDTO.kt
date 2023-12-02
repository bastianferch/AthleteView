package ase.athlete_view.domain.activity.pojo.dto

import ase.athlete_view.domain.activity.pojo.entity.Interval

class IntervalDTO(
    var id: Long? = null,
    var repeat: Int,
    var intervals: List<IntervalDTO>? = null,
    var step: StepDTO? = null
) {
    fun toEntity(): Interval {
        if (intervals?.isNotEmpty() == true) {
            return Interval(id, repeat, intervals?.toEntityList(), null)
        } else {
            return Interval(id, repeat, null, step?.toEntity())
        }
    }

    fun <E> List<E>.toEntityList(): List<Interval> {
        return intervals?.map { it.toEntity() } ?: emptyList()
    }

    override fun toString(): String {
        return "IntervalDTO(id=$id, repeat=$repeat, intervals=$intervals, step=$step)"
    }
}


