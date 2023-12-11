package ase.athlete_view.domain.zone.service.impl

import ase.athlete_view.common.exception.entity.ValidationException
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
    val zoneRepository: ZoneRepository,
    val athleteService: AthleteService
    ): ZoneService  {

    val log = KotlinLogging.logger {}
    override fun getAll(userId: Long): List<ZoneDto> {
        log.trace { "ZoneService.getAll($userId)" }

        val user = athleteService.getById(userId)
        return zoneRepository.findAllByUser(user).sortedBy { it.fromBPM }.map { zone -> zone.toDTO() }
    }

    @Transactional
    override fun edit(userId: Long, zones: List<ZoneDto>): List<ZoneDto> {
        log.trace { "ZoneService.edit($userId, $zones)" }

        val user = athleteService.getById(userId)
        val list: ArrayList<Zone> = arrayListOf()
        for (zone in zones){
            val entity = zone.toEntity()
            entity.user = user
            list.add(entity)
        }
        validateZones(list)
        return zoneRepository.saveAll(list).map { zone -> zone.toDTO() }
    }

    @Transactional
    override fun resetZones(userId: Long): List<ZoneDto> {
        log.trace { "ZoneService.resetZones($userId)" }

        val user = athleteService.getById(userId)
        zoneRepository.deleteByUser(user)
        val age = Period.between(user.dob, LocalDate.now()).years
        val list: List<Zone> = calcZones(age)
        list.forEach { it.user = user }

        zoneRepository.saveAll(list)
        return list.map { zone -> zone.toDTO() }
    }

    fun calcZones(age: Int): List<Zone> {
        // calculated with MHR method
        val amount = 5
        val startingPercent = 50
        val increment = (100 - startingPercent) / amount
        val MHR = 220 - age
        val names = listOf("","Recovery", "Aerobic", "Aerobic/Anaerobic", "Anaerobic", "Maximal")

        val list: ArrayList<Zone> = arrayListOf()

        for (i in 1..amount) {
            val from = MHR * (startingPercent + increment*(i-1)) / 100
            val to = MHR * (startingPercent + increment*i) / 100
            list.add(Zone(null, names[i], from, to, null))
        }
        list[0].fromBPM = 0

        return list
    }

    private fun validateZones(zones: List<Zone>) {

        val sorted = zones.sortedBy { it.fromBPM }
        var temp = 0
        for (zone in sorted) {
            if (zone.fromBPM > temp+1) throw ValidationException("Zones not covering full range")
            temp = zone.toBPM
        }
    }
}