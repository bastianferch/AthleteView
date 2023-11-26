package ase.athlete_view.domain.authentication.service

import ase.athlete_view.common.exception.entity.ValidationException
import ase.athlete_view.domain.authentication.dto.AthleteRegistrationDTO
import ase.athlete_view.domain.authentication.dto.TrainerRegistrationDTO
import ase.athlete_view.domain.user.pojo.entity.User

interface AuthValidationService {
    /**
     * Validates the trainer.
     *
     * @throws ValidationException if password, email or name was not presented.
     */
    fun validateTrainerDTO(trainerRegistrationDTO: TrainerRegistrationDTO)
    /**
     * Validates the athlete.
     *
     * @throws ValidationException if password, email or name was not presented.
     */
    fun validateAthleteDTO(athleteRegistrationDTO: AthleteRegistrationDTO)

    /**
     * checks the password -> #validatePassword
     * checks the athlete -> height and weight must be higher than 0. User should be at least 16 y.o.
     * All strings have to be below 255 chars.
     *
     * @throws ValidationException in those cases.
     */
    fun validateUser(user: User)

    /**
     * Validates the password.
     *
     * @throws ValidationException if the password is not provided or has a length less than 8 or bigger than 255.
     */
    fun validatePassword(pass: String)
}