package ase.athlete_view.domain.activity.pojo.dto

import ase.athlete_view.domain.activity.pojo.entity.Interval

class IntervalDTO(
    var id: Int? = null,
    var repeat: Int,
    var intervals: List<IntervalDTO>? = null,
    var step: StepDTO? = null
){
    fun toEntity(): Interval {
        return Interval(id, repeat, intervals?.toEntityList(), step?.toEntity())
    }

    fun <E> List<E>.toEntityList(): List<Interval> {
        return intervals?.map { it.toEntity() } ?: emptyList()
    }
}


