package ase.athlete_view.domain.authentication.service.impl

import ase.athlete_view.common.exception.entity.ConflictException
import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.common.exception.entity.ValidationException
import ase.athlete_view.domain.authentication.dto.AthleteRegistrationDTO
import ase.athlete_view.domain.authentication.dto.RegistrationDTO
import ase.athlete_view.domain.authentication.dto.TrainerRegistrationDTO
import ase.athlete_view.domain.authentication.service.AuthValidationService
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.User
import ase.athlete_view.domain.user.service.UserService
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class AuthValidationServiceImpl: AuthValidationService {
    override fun validateTrainerDTO(trainerRegistrationDTO: TrainerRegistrationDTO) {
        this.validateRegistrationDTO(trainerRegistrationDTO)
    }

    override fun validateAthleteDTO(athleteRegistrationDTO: AthleteRegistrationDTO) {
        this.validateRegistrationDTO(athleteRegistrationDTO)
    }

    override fun validateUser(user: User) {
        this.validatePassword(user.password)
        if (user.name.length > 255){
            throw ValidationException("Name length must be below 255 characters.")
        }
        if (user.email.length > 255){
            throw ValidationException("Email length must be below 255 characters.")
        }
        if (user.country != null && user.country?.length!! > 255){
            throw ValidationException("Country length must be below 255 characters.")
        }
        if (user.zip != null && user.zip?.length!! > 255){
            throw ValidationException("Zip length must be below 255 characters.")
        }
        if (user is Athlete) {
            if (user.height <= 0) {
                throw ValidationException("Height should be bigger than 0m")
            }
            if (user.height > 3000) {
                //frontend sends height in mm
                throw ValidationException("Height cannot be bigger than 3m")
            }
            if (user.weight <= 0) {
                throw ValidationException("Weight should be bigger than 0kg")
            }
            if (user.weight > 700000) {
                // frontend sends weight in grams
                throw ValidationException("Weight cannot be bigger than 700kg")
            }
            // ToDo discussionable
            if (user.dob.isAfter(LocalDate.now().minusYears(16))) {
                throw ValidationException("User has to be at least 16 years old to use this app")
            }
        }
    }

    override fun validatePassword(pass: String) {
        if (pass.length < 8) {
            throw ValidationException("Password is too short")
        }
        if (pass.length > 255){
            throw ValidationException("password is too long.")
        }
    }

    private fun validateRegistrationDTO(user: RegistrationDTO) {
        if (user.password == null || user.email == null || user.name == null) {
            throw ValidationException("User must contain an email, password and the name")
        }
    }

}