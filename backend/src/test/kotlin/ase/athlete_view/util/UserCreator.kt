package ase.athlete_view.util

import ase.athlete_view.domain.authentication.dto.AthleteRegistrationDTO
import ase.athlete_view.domain.authentication.dto.LoginDTO
import ase.athlete_view.domain.authentication.dto.TrainerRegistrationDTO
import ase.athlete_view.domain.user.pojo.dto.AthleteDTO
import ase.athlete_view.domain.user.pojo.dto.PreferencesDTO
import ase.athlete_view.domain.user.pojo.dto.TrainerDTO
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.NotificationPreferenceType
import ase.athlete_view.domain.user.pojo.entity.Trainer
import java.time.LocalDate

class UserCreator {
    companion object {
        const val DEFAULT_TRAINER_ID: Long = 1L
        const val DEFAULT_TRAINER_EMAIL: String = "trainer@example.com"
        const val DEFAULT_NEW_TRAINER_EMAIL: String = "trainer-new@example.com"
        const val DEFAULT_TRAINER_NAME: String = "Trainer Name"
        const val DEFAULT_TRAINER_PASSWORD: String = "trainerpassword"
        const val DEFAULT_TRAINER_COUNTRY: String = "Trainer Country"
        const val DEFAULT_TRAINER_ZIP: String = "12345"
        const val DEFAULT_TRAINER_CODE: String = "123456"

        const val DEFAULT_ATHLETE_ID: Long = 2L
        const val DEFAULT_ATHLETE_EMAIL: String = "athlete@example.com"
        const val DEFAULT_NEW_ATHLETE_EMAIL: String = "athlete-new@example.com"
        const val DEFAULT_ATHLETE_NAME: String = "Athlete Name"
        const val DEFAULT_ATHLETE_PASSWORD: String = "athletepassword"
        const val DEFAULT_ATHLETE_COUNTRY: String = "Athlete Country"
        const val DEFAULT_ATHLETE_ZIP: String = "54321"
        val DEFAULT_ATHLETE_DOB: LocalDate = LocalDate.of(1990, 5, 15)
        const val DEFAULT_ATHLETE_HEIGHT: Int = 1755
        const val DEFAULT_ATHLETE_WEIGHT: Int = 70500

        const val DEFAULT_USER_TOKEN_UUID = "b127a6b4-8cf8-4795-8f4f-c8fa54fbe0ad"

        const val DEFAULT_NON_EXISTENT_USER_EMAIL: String = "i_do_not_exist@test.com"

        fun getAthleteLoginDto(): LoginDTO {
            return LoginDTO(email = DEFAULT_ATHLETE_EMAIL, password = DEFAULT_ATHLETE_PASSWORD)
        }

        fun getNonExistentUserLoginDto(): LoginDTO {
            return LoginDTO(email = DEFAULT_NON_EXISTENT_USER_EMAIL, password = DEFAULT_ATHLETE_PASSWORD)
        }

        fun getTrainerDto():TrainerDTO{
            return TrainerDTO(id = DEFAULT_TRAINER_ID, email = DEFAULT_TRAINER_EMAIL, name = DEFAULT_TRAINER_NAME,
                country = DEFAULT_TRAINER_COUNTRY, zip = DEFAULT_TRAINER_ZIP, code = DEFAULT_TRAINER_CODE, token = "")
        }

        fun getTrainer(): Trainer {
            return Trainer(id = DEFAULT_TRAINER_ID, email = DEFAULT_TRAINER_EMAIL, name = DEFAULT_TRAINER_NAME,
                password = DEFAULT_TRAINER_PASSWORD, country = DEFAULT_TRAINER_COUNTRY, zip = DEFAULT_TRAINER_ZIP,
                code = DEFAULT_TRAINER_CODE, athletes = mutableSetOf(), unacceptedAthletes = mutableSetOf())
        }

        fun getAthleteDTO(): AthleteDTO{
            return AthleteDTO(id = DEFAULT_ATHLETE_ID, email = DEFAULT_ATHLETE_EMAIL, name = DEFAULT_ATHLETE_NAME,
                country = DEFAULT_ATHLETE_COUNTRY, zip = DEFAULT_ATHLETE_ZIP, dob = DEFAULT_ATHLETE_DOB,
                height = DEFAULT_ATHLETE_HEIGHT, weight = DEFAULT_ATHLETE_WEIGHT, trainer = getTrainerDto(), token = "", trainerToBe = null)
        }

        fun getAthlete(id:Long?): Athlete {
            return Athlete(
                id = id ?: DEFAULT_ATHLETE_ID, email = DEFAULT_ATHLETE_EMAIL, name = DEFAULT_ATHLETE_NAME,
                password = DEFAULT_ATHLETE_PASSWORD, country = DEFAULT_ATHLETE_COUNTRY, zip = DEFAULT_ATHLETE_ZIP,
                dob = DEFAULT_ATHLETE_DOB, height = DEFAULT_ATHLETE_HEIGHT, weight = DEFAULT_ATHLETE_WEIGHT,
                trainer = getTrainer(), trainerToBe = null
            )
        }

        fun getAthleteRegistrationDTO(code: String?): AthleteRegistrationDTO{
            return AthleteRegistrationDTO(
                email = DEFAULT_NEW_ATHLETE_EMAIL, name = DEFAULT_ATHLETE_NAME,
                password = DEFAULT_ATHLETE_PASSWORD, country = DEFAULT_ATHLETE_COUNTRY, zip = DEFAULT_ATHLETE_ZIP,
                dob = DEFAULT_ATHLETE_DOB, height = DEFAULT_ATHLETE_HEIGHT, weight = DEFAULT_ATHLETE_WEIGHT,
                code = code ?: "abcdef"
            )
        }

        fun getTrainerRegistrationDTO(): TrainerRegistrationDTO {
            return TrainerRegistrationDTO(
                email = DEFAULT_NEW_TRAINER_EMAIL, name = DEFAULT_TRAINER_NAME,
                password = DEFAULT_TRAINER_PASSWORD, country = DEFAULT_TRAINER_COUNTRY, zip = DEFAULT_TRAINER_ZIP
            )
        }

        // athlete as a user dto related to the #getAthtlete(id: Long?)
        fun getAthleteUser(): UserDTO {
            return UserDTO(
                id = DEFAULT_ATHLETE_ID, email = DEFAULT_ATHLETE_EMAIL, name = DEFAULT_ATHLETE_NAME,
                password = null, userType = "athlete", token = "legit-jwt-token"
            )
        }

        // trainer as a user dto related to the #getTrainer()
        fun getTrainerUser(): UserDTO {
            return UserDTO(
                id = DEFAULT_TRAINER_ID, email = DEFAULT_TRAINER_EMAIL, name = DEFAULT_TRAINER_NAME,
                password = null, userType = "trainer", token = "legit-jwt-token"
            )
        }

        fun getPreferencesDto(): PreferencesDTO {
            return PreferencesDTO(
                emailNotifications = true,
                commentNotifications = NotificationPreferenceType.NONE,
                ratingNotifications = NotificationPreferenceType.NONE,
                otherNotifications = NotificationPreferenceType.NONE,
                shareHealthWithTrainer = false
            )
        }

    }
}
