package ase.athlete_view.domain.user.service

import ase.athlete_view.common.exception.entity.ConflictException
import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.domain.user.pojo.dto.AthleteDTO
import ase.athlete_view.domain.user.pojo.dto.TrainerDTO
import ase.athlete_view.domain.user.pojo.entity.User

interface UserService {
    /**
     * Simple repository save.
     */
    fun save(user: User): User
    fun saveAll(users: List<User>): List<User>

    /**
     * Simple repository get.
     * @throws NotFoundException if user was not found.
     */
    fun getByEmail(string: String): User

    /**
     * Simple repository get.
     * @throws NotFoundException if user was not found.
     */
    fun getById(id: Long): User

    /**
     * Updates the trainer without id, email and password.
     * @throws ConflictException if user is trying to update the wrong user type (athlete)
     */
    fun updateTrainer(trainerDTO: TrainerDTO)

    /**
     * Updates the athlete without id, email and password.
     * @throws ConflictException if user is trying to update the wrong user type (trainer)
     */
    fun updateAthlete(athleteDTO: AthleteDTO)
}
