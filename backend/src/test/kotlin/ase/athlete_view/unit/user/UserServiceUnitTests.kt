package ase.athlete_view.unit.user

import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.domain.user.persistence.UserRepository
import ase.athlete_view.domain.user.pojo.entity.Preferences
import ase.athlete_view.domain.user.pojo.entity.User
import ase.athlete_view.domain.user.service.UserService
import ase.athlete_view.util.UserCreator
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import java.util.*


@SpringBootTest
@ActiveProfiles("test")
class UserServiceUnitTests {
    @MockkBean
    private lateinit var userRepo: UserRepository

    @Autowired
    private lateinit var userService: UserService

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

    @Test
    fun getPreferences_returnsPreferences() {
        //repo returns athlete with preferences
        val athlete = UserCreator.getAthlete(null)
        val originalPrefs = Preferences(athlete.id)
        athlete.preferences = originalPrefs
        every { userRepo.findById(any()) } returns Optional.of(athlete)

        // get preferences
        val prefs = userService.getPreferences(athlete.toUserDTO())
        verify(exactly = 1) { userRepo.findById(any()) }

        // should be the same as original preferences
        assertAll(
            { assertNotNull(prefs) },
            { assertEquals(prefs?.emailNotifications, originalPrefs.emailNotifications) },
            { assertEquals(prefs?.commentNotifications, originalPrefs.commentNotifications) },
            { assertEquals(prefs?.ratingNotifications, originalPrefs.ratingNotifications) },
            { assertEquals(prefs?.otherNotifications, originalPrefs.otherNotifications) },
        )
    }

    @Test
    fun updatePreferences_returnsNewPreferences() {
        //repo returns athlete with preferences
        val athlete = UserCreator.getAthlete(null)
        val originalPrefs = Preferences(athlete.id)
        athlete.preferences = originalPrefs
        every { userRepo.findById(any()) } returns Optional.of(athlete)
        every { userRepo.save(any()) } returns athlete

        //new preferences with other values than original ones
        val newPreferencesDto = UserCreator.getPreferencesDto()

        // get preferences
        val prefs = userService.patchPreferences(athlete.toUserDTO(), newPreferencesDto)
        verify(exactly = 1) { userRepo.findById(any()) }

        // should be the same as original preferences
        assertAll(
            { assertNotNull(prefs) },
            { assertEquals(prefs?.emailNotifications, newPreferencesDto.emailNotifications) },
            { assertEquals(prefs?.commentNotifications, newPreferencesDto.commentNotifications) },
            { assertEquals(prefs?.ratingNotifications, newPreferencesDto.ratingNotifications) },
            { assertEquals(prefs?.otherNotifications, newPreferencesDto.otherNotifications) },
        )
    }

}