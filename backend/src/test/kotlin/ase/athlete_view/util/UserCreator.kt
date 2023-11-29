package ase.athlete_view.util

import ase.athlete_view.domain.authentication.dto.LoginDTO
import ase.athlete_view.domain.user.pojo.dto.AthleteDTO
import ase.athlete_view.domain.user.pojo.dto.TrainerDTO
import ase.athlete_view.domain.user.pojo.dto.UserDto
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.domain.user.pojo.entity.User
import java.time.LocalDate

class UserCreator {
    companion object {
        const val DEFAULT_USER_ID: Long         = 1L
        const val DEFAULT_USER_NAME: String     = "Testuser"
        const val DEFAULT_USER_PASSWORD: String = "password"
        const val DEFAULT_USER_EMAIL: String    = "a@b.com"
        const val DEFAULT_USER_ZIP: String      = "1337"
        const val DEFAULT_TOKEN_VALUE: String   = "token"
        const val DEFAULT_USER_COUNTRY: String  = "Austria"

        const val DEFAULT_TRAINER_ID: Long = 2L
        const val DEFAULT_TRAINER_EMAIL: String = "trainer@example.com"
        const val DEFAULT_TRAINER_NAME: String = "Trainer Name"
        const val DEFAULT_TRAINER_PASSWORD: String = "trainerpassword"
        const val DEFAULT_TRAINER_COUNTRY: String = "Trainer Country"
        const val DEFAULT_TRAINER_ZIP: String = "12345"
        const val DEFAULT_TRAINER_CODE: String = "123456"

        const val DEFAULT_ATHLETE_ID: Long = 3L
        const val DEFAULT_ATHLETE_EMAIL: String = "athlete@example.com"
        const val DEFAULT_ATHLETE_NAME: String = "Athlete Name"
        const val DEFAULT_ATHLETE_PASSWORD: String = "athletepassword"
        const val DEFAULT_ATHLETE_COUNTRY: String = "Athlete Country"
        const val DEFAULT_ATHLETE_ZIP: String = "54321"
        val DEFAULT_ATHLETE_DOB: LocalDate = LocalDate.of(1990, 5, 15)
        const val DEFAULT_ATHLETE_HEIGHT: Int = 1755
        const val DEFAULT_ATHLETE_WEIGHT: Int = 70500

        fun getUserDto(): UserDto {
            return UserDto(id = DEFAULT_USER_ID, name = DEFAULT_USER_NAME,
                email = DEFAULT_USER_EMAIL, password = DEFAULT_USER_PASSWORD,
                token = DEFAULT_TOKEN_VALUE)
        }

        fun getUser(): User {
            return User(id = DEFAULT_USER_ID, email = DEFAULT_USER_EMAIL,
                name = DEFAULT_USER_NAME, password = DEFAULT_USER_PASSWORD,
                country = DEFAULT_USER_COUNTRY, zip = DEFAULT_USER_ZIP)
        }

        fun getLoginDto(): LoginDTO {
            return LoginDTO(email = DEFAULT_USER_EMAIL, password = DEFAULT_USER_PASSWORD)
        }

        fun getTrainerDto():TrainerDTO{
            return TrainerDTO(id = DEFAULT_TRAINER_ID, email = DEFAULT_TRAINER_EMAIL, name = DEFAULT_TRAINER_NAME,
                country = DEFAULT_TRAINER_COUNTRY, zip = DEFAULT_TRAINER_ZIP, code = DEFAULT_TRAINER_CODE)
        }

        fun getTrainer(): Trainer{
            return Trainer(id = DEFAULT_TRAINER_ID, email = DEFAULT_TRAINER_EMAIL, name = DEFAULT_TRAINER_NAME,
                password = DEFAULT_TRAINER_PASSWORD, country = DEFAULT_TRAINER_COUNTRY, zip = DEFAULT_TRAINER_ZIP,
                code = DEFAULT_TRAINER_CODE)
        }

        fun getAthleteDTO(): AthleteDTO{
            return AthleteDTO(id = DEFAULT_ATHLETE_ID, email = DEFAULT_ATHLETE_EMAIL, name = DEFAULT_ATHLETE_NAME,
                country = DEFAULT_ATHLETE_COUNTRY, zip = DEFAULT_ATHLETE_ZIP, dob = DEFAULT_ATHLETE_DOB,
                height = DEFAULT_ATHLETE_HEIGHT, weight = DEFAULT_ATHLETE_WEIGHT, trainer = getTrainerDto())
        }

        fun getAthlete(): Athlete{
            return Athlete(id = DEFAULT_ATHLETE_ID, email = DEFAULT_ATHLETE_EMAIL, name = DEFAULT_ATHLETE_NAME,
                password = DEFAULT_ATHLETE_PASSWORD, country = DEFAULT_ATHLETE_COUNTRY, zip = DEFAULT_ATHLETE_ZIP,
                dob = DEFAULT_ATHLETE_DOB, height = DEFAULT_ATHLETE_HEIGHT, weight = DEFAULT_ATHLETE_WEIGHT,
                trainer = getTrainer())
        }
    }
}
