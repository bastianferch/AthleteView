package ase.athlete_view.domain.csp.controller

import ase.athlete_view.domain.csp.pojo.dto.CspDto
import ase.athlete_view.domain.csp.service.CspService
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RequestMapping("api/csp")
@RestController
class CspController(private val cspService: CspService) {

    private val logger = KotlinLogging.logger {}

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun postJob(
            @AuthenticationPrincipal user: UserDTO,
            @RequestBody cspDto: CspDto
    ) {
        logger.info { "POST CSP Scheduling job" }

        if (user.id != null) {
            cspService.accept(cspDto, user.id!!);
        }
    }

    @DeleteMapping
    fun deleteJob(@AuthenticationPrincipal user: UserDTO) {
        logger.info { "DELETE job for next week for ${user.id}" }
        if (user.id != null) {
            cspService.revertJob(user.id!!)
        }
    }

    @GetMapping
    fun checkJobExists(@AuthenticationPrincipal user: UserDTO): Boolean {
        logger.info { "GET if job for next week for ${user.id} exists" }
        if (user.id != null) {
            return cspService.getJob(user.id!!) != null
        }
        return false
    }
}
