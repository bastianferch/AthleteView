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
package ase.athlete_view.domain.health.controller

import ase.athlete_view.domain.health.pojo.dto.HealthDTO
import ase.athlete_view.domain.health.service.HealthService
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RequestMapping("api/health")
@RestController
class HealthController (private val healthService: HealthService){
    private val log = KotlinLogging.logger {}

    @PostMapping("/sync")
    @ResponseStatus(HttpStatus.CREATED)
    fun syncWithMockServer(@AuthenticationPrincipal user: UserDTO) {
        log.info { "POST | syncWithMockServer()" }
        this.healthService.syncWithMockServer(user.id!!)
    }

    // cannot make GET api/health since that would conflict with config kubernetes health endpoint
    @GetMapping("/stats")
    fun getHealth(@AuthenticationPrincipal user: UserDTO): HealthDTO {
        log.info { "GET | getHealth()" }
        return this.healthService.getAllByCurrentUser(user.id!!).maxByOrNull { health -> health.date }?.toDTO() ?: HealthDTO(null, LocalDate.MIN,-1,-1,-1)
    }

    @GetMapping("/stats/{id}")
    fun getHealthFromAthlete(@AuthenticationPrincipal user: UserDTO, @PathVariable id: Long): HealthDTO {
        log.info { "GET | getHealthFromAthlete($id)" }
        return this.healthService.getAllFromAthlete(id, user.id!!).maxByOrNull { health -> health.date }?.toDTO() ?: HealthDTO(null, LocalDate.MIN,-1,-1,-1)
    }
}
