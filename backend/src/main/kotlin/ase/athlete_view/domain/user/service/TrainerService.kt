package ase.athlete_view.domain.user.service

import ase.athlete_view.domain.user.pojo.dto.UserDTO
import ase.athlete_view.domain.user.pojo.entity.Trainer

interface TrainerService {
    /**
     * @return true if there is a trainer with a given code.
     */
    fun getByCode(code: String): Trainer?

    /**
     * resets the code of a trainer.
     */
    fun resetCode(user: UserDTO)

    /**
     * Accepts an athlete by a trainer.
     *
     * @param userDTO the trainer.
     * @param athleteDTO the athlete.
     */
    fun acceptAthlete(userDTO: UserDTO, id: Long)
}
