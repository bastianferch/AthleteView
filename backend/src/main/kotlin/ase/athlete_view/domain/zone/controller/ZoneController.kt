package ase.athlete_view.domain.zone.controller

import ase.athlete_view.domain.user.pojo.dto.UserDTO
import ase.athlete_view.domain.zone.pojo.dto.ZoneDto
import ase.athlete_view.domain.zone.service.ZoneService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RequestMapping("api/zones")
@RestController
class ZoneController(val zoneService: ZoneService) {

    private val logger = KotlinLogging.logger {}

    @PutMapping
    fun putZones(@RequestBody zones: List<ZoneDto>, @AuthenticationPrincipal userDto: UserDTO): List<ZoneDto> {

        logger.info { "PUT zones: $zones by ${userDto.name}" }
        return zoneService.edit(userDto.id!!, zones)
    }

    @GetMapping
    fun getZones(@AuthenticationPrincipal userDto: UserDTO): List<ZoneDto> {

        logger.info { "GET zones by $userDto" }
        return zoneService.getAll(userDto.id!!)
    }

    @DeleteMapping
    fun resetZones(@AuthenticationPrincipal userDto: UserDTO): List<ZoneDto> {

        logger.info { "DELETE zones by $userDto" }
        return zoneService.resetZones(userDto.id!!)
    }
}