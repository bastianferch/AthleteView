package ase.athlete_view.domain.zone.service

import ase.athlete_view.domain.zone.pojo.dto.ZoneDto
import ase.athlete_view.domain.zone.pojo.entity.Zone

interface ZoneService {

    /**
     * returns all Zones for athlete with the given id
     */
    fun getAll(userId: Long): List<ZoneDto>

    /**
     * updates Zones for athlete with the given id
     */
    fun edit(userId: Long, zones: List<ZoneDto>): List<ZoneDto>

    /**
     * resets Zones for athlete with given id
     * will delete saved Zones and create new ones based on maximum heart rate of the athlete
     * maximum heart rate can be given as a parameter, otherwise calculated with a heuristic based on age
     */
    fun resetZones(userId: Long, maxHR: Int? = null): List<ZoneDto>

    /**
     * Creates a list of 5 Zones going from 50 to 100 percent of given maximum heart rate
     */
    fun calcZones(maxHR: Int): List<Zone>
}