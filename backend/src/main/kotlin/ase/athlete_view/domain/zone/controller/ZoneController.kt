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

    private val log = KotlinLogging.logger {}

    @PutMapping
    fun putZones(@RequestBody zones: List<ZoneDto>, @AuthenticationPrincipal userDto: UserDTO): List<ZoneDto> {

        log.info { "PUT | putZones($zones)" }
        return zoneService.edit(userDto.id!!, zones)
    }

    @GetMapping
    fun getZones(@AuthenticationPrincipal userDto: UserDTO): List<ZoneDto> {

        log.info { "GET | getZones()" }
        return zoneService.getAll(userDto.id!!)
    }

    @DeleteMapping
    fun resetZones(@RequestParam(required = false, name = "maxHR") maxHR: Int?,
                   @AuthenticationPrincipal userDto: UserDTO): List<ZoneDto> {

        log.info { "DELETE | resetZones($maxHR)" }
        return zoneService.resetZones(userDto.id!!, maxHR)
    }
}
