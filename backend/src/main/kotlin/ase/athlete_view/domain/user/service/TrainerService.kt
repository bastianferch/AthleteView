package ase.athlete_view.domain.user.service

import ase.athlete_view.domain.user.pojo.dto.UserDTO
import ase.athlete_view.domain.user.pojo.entity.Trainer

interface TrainerService {
    /**
     * @return true if there is a trainer with a given code.
     */
    fun getByCode(code: String): Trainer?
    fun resetCode(user: UserDTO)
}
