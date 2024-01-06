package ase.athlete_view.unit.user

import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.domain.user.persistence.UserRepository
import ase.athlete_view.domain.user.pojo.entity.User
import ase.athlete_view.domain.user.service.UserService
import ase.athlete_view.domain.user.service.mapper.UserMapper
import ase.athlete_view.util.UserCreator
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles


@SpringBootTest
@ActiveProfiles("test")
class UserServiceUnitTests {
    @MockkBean
    private lateinit var userRepo: UserRepository

    @Autowired

    private lateinit var userService: UserService

    @MockkBean
    private lateinit var userMapper: UserMapper

    @Test
    fun saveUser() {
        val userDummy = UserCreator.getAthlete(null)
        every { userRepo.save(any()) } returns userDummy

        val persistedUser = userService.save(userDummy)
        verify(exactly = 1) { userRepo.save(userDummy) }

        assertAll(
            { assertThat(persistedUser.id).isEqualTo(UserCreator.DEFAULT_ATHLETE_ID) },
            { assertThat(persistedUser.email).isEqualTo(UserCreator.DEFAULT_ATHLETE_EMAIL) },
            { assertThat(persistedUser.password).isEqualTo(UserCreator.DEFAULT_ATHLETE_PASSWORD) },
            { assertThat(persistedUser.zip).isEqualTo(UserCreator.DEFAULT_ATHLETE_ZIP) },
            { assertThat(persistedUser.country).isEqualTo(UserCreator.DEFAULT_ATHLETE_COUNTRY) },
            { assertThat(persistedUser.name).isEqualTo(UserCreator.DEFAULT_ATHLETE_NAME) }
        )
    }

    @Test
    fun saveAllUsers() {
        val athleteDummy = UserCreator.getAthlete(null)
        val trainerDummy = UserCreator.getTrainer()
        val userList = listOf(athleteDummy, trainerDummy)
        every { userRepo.saveAll(any<List<User>>()) } returns userList

        val persistedUsers = userService.saveAll(userList)
        verify(exactly = 1) { userRepo.saveAll(userList) }

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
        every { userRepo.findByEmail("a@b.com") } returns UserCreator.getAthlete(null)

        val usrData = userService.getByEmail("a@b.com")
        verify(exactly = 1) { userRepo.findByEmail("a@b.com") }

        assertAll(
            { assertThat(usrData.id).isEqualTo(UserCreator.DEFAULT_ATHLETE_ID) },
            { assertThat(usrData.email).isEqualTo(UserCreator.DEFAULT_ATHLETE_EMAIL) },
            { assertThat(usrData.password).isEqualTo(UserCreator.DEFAULT_ATHLETE_PASSWORD) },
            { assertThat(usrData.zip).isEqualTo(UserCreator.DEFAULT_ATHLETE_ZIP) },
            { assertThat(usrData.country).isEqualTo(UserCreator.DEFAULT_ATHLETE_COUNTRY) },
            { assertThat(usrData.name).isEqualTo(UserCreator.DEFAULT_ATHLETE_NAME) }
        )
    }

    @Test
    fun getByEmailWithWrongEmail_ThrowsNotFound() {
        every { userRepo.findByEmail("a@b.com") } returns null

        assertThrows<NotFoundException> { userService.getByEmail("a@b.com") }
        verify(exactly = 1) { userRepo.findByEmail("a@b.com") }
    }

    @Test
    fun getById_ReturnsUser() {
        every { userRepo.findByIdOrNull(1) } returns UserCreator.getAthlete(null)

        val usrData = userService.getById(1)
        verify(exactly = 1) { userRepo.findByIdOrNull(1) }

        assertAll(
            { assertThat(usrData.id).isEqualTo(UserCreator.DEFAULT_ATHLETE_ID) },
            { assertThat(usrData.email).isEqualTo(UserCreator.DEFAULT_ATHLETE_EMAIL) },
            { assertThat(usrData.password).isEqualTo(UserCreator.DEFAULT_ATHLETE_PASSWORD) },
            { assertThat(usrData.zip).isEqualTo(UserCreator.DEFAULT_ATHLETE_ZIP) },
            { assertThat(usrData.country).isEqualTo(UserCreator.DEFAULT_ATHLETE_COUNTRY) },
            { assertThat(usrData.name).isEqualTo(UserCreator.DEFAULT_ATHLETE_NAME) }
        )

    }

    @Test
    fun getByIdWithWrongId_ThrowsNotFound() {
        every { userRepo.findByIdOrNull(1) } returns null

        assertThrows<NotFoundException> { userService.getById(1) }
        verify(exactly = 1) { userRepo.findByIdOrNull(1) }
    }

}