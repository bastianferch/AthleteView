package ase.athlete_view.domain.zone.service

import ase.athlete_view.domain.zone.persistence.ZoneRepository
import ase.athlete_view.domain.zone.pojo.dto.ZoneDto
import ase.athlete_view.domain.zone.pojo.entity.Zone

interface ZoneService {

    fun getAll(userId: Long): List<ZoneDto>

    fun edit(userId: Long, zones: List<ZoneDto>): List<ZoneDto>

    fun resetZones(userId: Long): List<ZoneDto>

    fun calcZones(age: Int): List<Zone>
}