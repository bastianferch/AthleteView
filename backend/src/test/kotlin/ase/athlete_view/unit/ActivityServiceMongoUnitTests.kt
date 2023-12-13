package ase.athlete_view.unit

import ase.athlete_view.AthleteViewApplication
import ase.athlete_view.common.exception.fitimport.DuplicateFitFileException
import ase.athlete_view.common.exception.fitimport.InvalidFitFileException
import ase.athlete_view.domain.activity.persistence.ActivityRepository
import ase.athlete_view.domain.activity.pojo.entity.Activity
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.user.persistence.UserRepository
import ase.athlete_view.util.ActivityCreator
import ase.athlete_view.util.UserCreator
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.absolute

@SpringBootTest(classes = [AthleteViewApplication::class])
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
class ActivityServiceMongoUnitTests {
    @Autowired
    private lateinit var activityService: ActivityService

    @MockkBean
    private lateinit var userRepository: UserRepository

    @MockkBean
    private lateinit var activityRepository: ActivityRepository


    @Test
    @Transactional
    fun uploadingValidIFitFile_shouldSucceed() {
        every { userRepository.findById(any<Long>()) } returns Optional.of(UserCreator.getAthlete())
        every { activityRepository.save(any<Activity>() )} returns ActivityCreator.getDefaultActivity()

        val filePath = Paths.get("src/test/resources/fit-files/valid_file.fit").absolute()
        val name = "test.fit"
        val content = "text/plain"
        val byteContent = Files.readAllBytes(filePath)
        val resultFile = MockMultipartFile(name, name, content, byteContent)

        assertDoesNotThrow { activityService.importActivity(arrayListOf(resultFile), 1L) }
    }

    @Test
    @Transactional
    fun uploadingEmptyButValidIFitFile_shouldSucceed() {
        every { userRepository.findById(any<Long>()) } returns Optional.of(UserCreator.getAthlete())

        val filePath = Paths.get("src/test/resources/fit-files/valid_file_2.fit").absolute()
        val name = "test.fit"
        val content = "text/plain"
        val byteContent = Files.readAllBytes(filePath)
        val resultFile = MockMultipartFile(name, name, content, byteContent)

        assertDoesNotThrow { activityService.importActivity(arrayListOf(resultFile), 0L) }
    }

    @Test
    @Transactional
    fun uploadingInvalidIFitFile_shouldThrowException() {
        every { userRepository.findById(any<Long>()) } returns Optional.of(UserCreator.getAthlete())

        val filePath = Paths.get("src/test/resources/fit-files/empty_file.fit").absolute()
        val name = "login"
        val content = "text/plain"
        val byteContent = Files.readAllBytes(filePath)
        val resultFile = MockMultipartFile(name, name, content, byteContent)

        // userid doesn't matter bc mockked
        assertThrows<InvalidFitFileException> { activityService.importActivity(arrayListOf(resultFile), 1L) }
    }

    @Test
    @Transactional
    fun uploadingDuplicateFile_shouldThrowDuplicateFitFileException() {
        every { userRepository.findById(any<Long>()) } returns Optional.of(UserCreator.getAthlete())
        every { activityRepository.save(any<Activity>()) } returns ActivityCreator.getDefaultActivity()

        val filePath = Paths.get("src/test/resources/fit-files/valid_file.fit").absolute()
        val name = "test.fit"
        val content = "text/plain"
        val byteContent = Files.readAllBytes(filePath)
        val resultFile = MockMultipartFile(name, name, content, byteContent)

        // userid doesn't matter bc mockked
        activityService.importActivity(arrayListOf(resultFile), 1L)
        assertThrows<DuplicateFitFileException> {
            activityService.importActivity(arrayListOf(resultFile), 1L)
        }
    }
}