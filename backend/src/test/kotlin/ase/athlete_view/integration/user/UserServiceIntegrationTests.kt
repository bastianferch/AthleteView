package ase.athlete_view.integration.user

import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.domain.user.persistence.UserRepository
import ase.athlete_view.domain.user.service.UserService
import ase.athlete_view.domain.user.service.mapper.UserMapper
import ase.athlete_view.util.TestBase
import ase.athlete_view.util.UserCreator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class UserServiceIntegrationTests : TestBase() {
    @Autowired
    private lateinit var userRepo: UserRepository

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var userMapper: UserMapper

    @Test
    fun saveAthleteWithoutTrainer() {
        val athleteDummy = UserCreator.getAthlete(0)
        athleteDummy.trainer = null

        val persistedUser = userService.save(athleteDummy)

        assertAll(
            { assertThat(persistedUser.email).isEqualTo(UserCreator.DEFAULT_ATHLETE_EMAIL) },
            { assertThat(persistedUser.password).isEqualTo(UserCreator.DEFAULT_ATHLETE_PASSWORD) },
            { assertThat(persistedUser.zip).isEqualTo(UserCreator.DEFAULT_ATHLETE_ZIP) },
            { assertThat(persistedUser.country).isEqualTo(UserCreator.DEFAULT_ATHLETE_COUNTRY) },
            { assertThat(persistedUser.name).isEqualTo(UserCreator.DEFAULT_ATHLETE_NAME) }
        )
    }

    @Test
    fun saveTrainerWithoutAthlete() {
        val trainerDummy = UserCreator.getTrainer()

        val persistedUser = userService.save(trainerDummy)

        assertAll(
            { assertThat(persistedUser.email).isEqualTo(UserCreator.DEFAULT_TRAINER_EMAIL) },
            { assertThat(persistedUser.password).isEqualTo(UserCreator.DEFAULT_TRAINER_PASSWORD) },
            { assertThat(persistedUser.zip).isEqualTo(UserCreator.DEFAULT_TRAINER_ZIP) },
            { assertThat(persistedUser.country).isEqualTo(UserCreator.DEFAULT_TRAINER_COUNTRY) },
            { assertThat(persistedUser.name).isEqualTo(UserCreator.DEFAULT_TRAINER_NAME) }
        )
    }

    @Test
    fun saveTrainerWithAthlete() {
        val athleteWithTrainer = UserCreator.getAthlete(null)
        athleteWithTrainer.trainer?.athletes = mutableSetOf(athleteWithTrainer)
        athleteWithTrainer.trainer?.id = null;
        athleteWithTrainer.id = null;

        val persistedUser = userService.save(athleteWithTrainer)

        assertAll(
            { assertThat(persistedUser.email).isEqualTo(UserCreator.DEFAULT_ATHLETE_EMAIL) },
            { assertThat(persistedUser.password).isEqualTo(UserCreator.DEFAULT_ATHLETE_PASSWORD) },
            { assertThat(persistedUser.zip).isEqualTo(UserCreator.DEFAULT_ATHLETE_ZIP) },
            { assertThat(persistedUser.country).isEqualTo(UserCreator.DEFAULT_ATHLETE_COUNTRY) },
            { assertThat(persistedUser.name).isEqualTo(UserCreator.DEFAULT_ATHLETE_NAME) }
        )
    }

    @Test
    fun saveAllUsers() {
        val athleteDummy = UserCreator.getAthlete(0)
        athleteDummy.trainer = null
        val trainerDummy = UserCreator.getTrainer()
        trainerDummy.id = 0
        val userList = listOf(athleteDummy, trainerDummy)
        val persistedUsers = userService.saveAll(userList)

        assertAll(
            { assertThat(persistedUsers).hasSize(2) },
            { assertThat(persistedUsers[0].email).isEqualTo(UserCreator.DEFAULT_ATHLETE_EMAIL) },
            { assertThat(persistedUsers[1].email).isEqualTo(UserCreator.DEFAULT_TRAINER_EMAIL) },
            { assertThat(persistedUsers[0].name).isEqualTo(UserCreator.DEFAULT_ATHLETE_NAME) },
            { assertThat(persistedUsers[1].name).isEqualTo(UserCreator.DEFAULT_TRAINER_NAME) }
        )
    }

    @Test
    fun getByEmail_ReturnsUser() {
        val email = "test@getByEmail.com"

        assertThrows<NotFoundException> { userService.getByEmail(email) }
        val user = UserCreator.getAthlete(null)
        user.email = email
        this.userRepo.save(user)
        val usrData = userService.getByEmail(email)

        assertAll(
            { assertThat(usrData.email).isEqualTo(email) },
            { assertThat(usrData.password).isEqualTo(UserCreator.DEFAULT_ATHLETE_PASSWORD) },
            { assertThat(usrData.zip).isEqualTo(UserCreator.DEFAULT_ATHLETE_ZIP) },
            { assertThat(usrData.country).isEqualTo(UserCreator.DEFAULT_ATHLETE_COUNTRY) },
            { assertThat(usrData.name).isEqualTo(UserCreator.DEFAULT_ATHLETE_NAME) }
        )
    }

    @Test
    fun getById_ReturnsUser() {
        val email = "test@getByEmail.com"
        assertThrows<NotFoundException> { userService.getByEmail(email) }
        val user = UserCreator.getAthlete(null)
        user.email = email
        val persistedUser = this.userRepo.save(user)
        val usrData = persistedUser.id?.let { userService.getById(it) }

        assertAll(
            { assertThat(usrData?.email).isEqualTo(email) },
            { assertThat(usrData?.password).isEqualTo(UserCreator.DEFAULT_ATHLETE_PASSWORD) },
            { assertThat(usrData?.zip).isEqualTo(UserCreator.DEFAULT_ATHLETE_ZIP) },
            { assertThat(usrData?.country).isEqualTo(UserCreator.DEFAULT_ATHLETE_COUNTRY) },
            { assertThat(usrData?.name).isEqualTo(UserCreator.DEFAULT_ATHLETE_NAME) }
        )
    }
}