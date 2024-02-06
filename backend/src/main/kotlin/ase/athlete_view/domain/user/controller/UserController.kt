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
package ase.athlete_view.domain.user.controller

import ase.athlete_view.common.exception.entity.ForbiddenException
import ase.athlete_view.domain.user.pojo.dto.AthleteDTO
import ase.athlete_view.domain.user.pojo.dto.PreferencesDTO
import ase.athlete_view.domain.user.pojo.dto.TrainerDTO
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.domain.user.service.AthleteService
import ase.athlete_view.domain.user.service.TrainerService
import ase.athlete_view.domain.user.service.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/user")
class UserController (private val trainerService: TrainerService,
    private val userService: UserService, private val athleteService: AthleteService
) {
    val log = KotlinLogging.logger {}

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    fun get(@AuthenticationPrincipal userDTO: UserDTO): UserDTO {
        log.info { "GET | get()" }
        val user = userDTO.id?.let { this.userService.getById(it) } ?: throw ForbiddenException("You cannot get your profile")
        return when (user) {
            is Athlete -> {
                user.toAthleteDto()
            }

            is Trainer -> {
                user.toDto()
            }

            else -> {
                user.toUserDTO()
            }
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/athlete")
    fun getAthletes(@AuthenticationPrincipal userDTO: UserDTO): List<AthleteDTO> {
        log.info { "GET | getAthletes()" }

        val athleteDtos = mutableListOf<AthleteDTO>()
        val athletes = userDTO.id?.let { this.athleteService.getByTrainerId(it) }
        if (athletes != null) {
            for (i in athletes.indices) {
                athleteDtos.add(athletes[i].toAthleteDto())
            }
        }
        return athleteDtos
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/trainer/athlete")
    fun acceptAthlete(@AuthenticationPrincipal userDTO: UserDTO, @RequestBody id: Long) {
        log.info { "POST | acceptAthlete($id)" }
        this.trainerService.acceptAthlete(userDTO, id)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/athlete")
    fun updateAthlete(@AuthenticationPrincipal userDTO: UserDTO, @RequestBody athleteDTO: AthleteDTO) {
        log.info { "PUT | updateAthlete($athleteDTO)" }
        athleteDTO.email = userDTO.email
        this.userService.updateAthlete(athleteDTO)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/trainer")
    fun updateTrainer(@AuthenticationPrincipal userDTO: UserDTO, @RequestBody trainerDTO: TrainerDTO) {
        log.info { "PUT | updateTrainer($trainerDTO)" }
        trainerDTO.email = userDTO.email
        this.userService.updateTrainer(trainerDTO)
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/trainer/invitation")
    fun inviteAthlete(@AuthenticationPrincipal userDTO: UserDTO, @RequestBody emailList: List<String>) {
        log.info { "POST | inviteAthlete($emailList)" }
        this.trainerService.inviteAthletes(userDTO.id!!, emailList)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/trainer/code")
    fun resetCode(@AuthenticationPrincipal userDTO: UserDTO) {
        log.info { "POST | resetCode()" }
        this.trainerService.resetCode(userDTO)
    }

    @GetMapping("/preferences")
    fun getPreferences(@AuthenticationPrincipal user: UserDTO): PreferencesDTO? {
        log.info { "GET | getPreferences()" }
        return this.userService.getPreferences(user)?.toDTO()
    }

    @PatchMapping("/preferences")
    fun patchPreferences(@AuthenticationPrincipal user: UserDTO, @RequestBody preferencesDTO: PreferencesDTO): PreferencesDTO? {
        log.info { "PATCH | patchPreferences($preferencesDTO)" }
        return this.userService.patchPreferences(user, preferencesDTO)?.toDTO()
    }
}
