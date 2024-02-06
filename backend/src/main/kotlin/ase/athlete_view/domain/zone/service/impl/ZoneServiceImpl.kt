/*-
 * #%L
 * athlete_view
 * %%
 * Copyright (C) 2023 - 2024 TU Wien INSO ASE GROUP 5 WS2023
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
package ase.athlete_view.domain.zone.service.impl

import ase.athlete_view.common.exception.entity.ValidationException
import ase.athlete_view.common.sanitization.Sanitizer
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.service.AthleteService
import ase.athlete_view.domain.zone.persistence.ZoneRepository
import ase.athlete_view.domain.zone.pojo.dto.ZoneDto
import ase.athlete_view.domain.zone.pojo.dto.toEntity
import ase.athlete_view.domain.zone.pojo.entity.Zone
import ase.athlete_view.domain.zone.pojo.entity.toDTO
import ase.athlete_view.domain.zone.service.ZoneService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Period

@Service
class ZoneServiceImpl(
    private val zoneRepository: ZoneRepository,
    private val athleteService: AthleteService,
    private val sanitizer: Sanitizer,
): ZoneService  {

    val log = KotlinLogging.logger {}
    override fun getAll(userId: Long): List<ZoneDto> {
        log.trace { "S | getAll($userId)" }

        val user = athleteService.getById(userId)
        return zoneRepository.findAllByUser(user).sortedBy { it.fromBPM }.map { zone -> zone.toDTO() }
    }

    @Transactional
    override fun edit(userId: Long, zones: List<ZoneDto>): List<ZoneDto> {
        log.trace { "S | edit($userId, $zones)" }

        val user = athleteService.getById(userId)
        val list: ArrayList<Zone> = arrayListOf()
        for (zone in zones){
            val entity = sanitizer.sanitizeZoneDTO(zone).toEntity()
            entity.user = user
            list.add(entity)
        }
        validateZones(list)
        zoneRepository.deleteByUser(user)
        return zoneRepository.saveAll(list).map { zone -> zone.toDTO() }
    }

    @Transactional
    override fun resetZones(userId: Long, maxHR: Int?): List<ZoneDto> {
        log.trace { "S | resetZones($userId, $maxHR)" }

        val user = athleteService.getById(userId)
        zoneRepository.deleteByUser(user)

        val list: List<Zone> = calcZones(maxHR?: calcMaxHR(user))
        list.forEach { it.user = user }

        zoneRepository.saveAll(list)
        return list.map { zone -> zone.toDTO() }
    }

    override fun calcZones(maxHR: Int): List<Zone> {
        log.trace { "S | calcZones($maxHR)" }
        // calculated with MHR method
        val amount = 5
        val startingPercent = 50
        val increment = (100 - startingPercent) / amount
        val names = listOf("","Recovery", "Aerobic", "Aerobic/Anaerobic", "Anaerobic", "Maximal")

        val list: ArrayList<Zone> = arrayListOf()

        for (i in 1..amount) {
            val from = (maxHR * (startingPercent + increment*(i-1)) / 100) + 1
            val to = maxHR * (startingPercent + increment*i) / 100
            list.add(Zone(null, names[i], from, to, null))
        }
        list[0].fromBPM = 0

        return list
    }

    private fun calcMaxHR(user: Athlete): Int {
        log.trace { "S |  calcMaxHR($user)" }

        val age = Period.between(user.dob, LocalDate.now()).years
        return 220 - age
    }

    private fun validateZones(zones: List<Zone>) {
        log.trace { "S | validateZones($zones)" }

        val sorted = zones.sortedBy { it.fromBPM }
        var temp = -1
        for (zone in sorted) {
            if (zone.fromBPM < 0 || zone.fromBPM > zone.toBPM) throw ValidationException("Invalid zones")
            if (zone.fromBPM != temp+1) throw ValidationException("Zones not covering full range")
            temp = zone.toBPM
        }
    }
}
