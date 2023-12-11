package ase.athlete_view.domain.zone.pojo.dto

import ase.athlete_view.domain.zone.pojo.entity.Zone

class ZoneDto (
    var id: Long?,
    val name: String,
    var fromBPM: Int,
    val toBPM: Int
) {
    override fun toString(): String {
        return "ZoneDto(id=$id, name='$name', fromBPM=$fromBPM, toBPM=$toBPM)"
    }
}

fun ZoneDto.toEntity(): Zone {
    return Zone(id,name,fromBPM,toBPM, null)
}