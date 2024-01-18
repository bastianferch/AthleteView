package ase.athlete_view.unit

import ase.athlete_view.AthleteViewApplication
import ase.athlete_view.common.exception.fitimport.DuplicateFitFileException
import ase.athlete_view.common.exception.fitimport.InvalidFitFileException
import ase.athlete_view.domain.activity.persistence.ActivityRepository
import ase.athlete_view.domain.activity.persistence.PlannedActivityRepository
import ase.athlete_view.domain.activity.pojo.entity.Activity
import ase.athlete_view.domain.activity.pojo.util.ActivityType
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.user.persistence.UserRepository
import ase.athlete_view.util.ActivityCreator
import ase.athlete_view.util.UserCreator
import ase.athlete_view.util.WithCustomMockUser
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
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
    private lateinit var plannedActivityRepo: PlannedActivityRepository

    @MockkBean
    private lateinit var userRepository: UserRepository

    @MockkBean
    private lateinit var activityRepository: ActivityRepository


    @Test
    @Transactional
    fun uploadingValidIFitFile_shouldSucceed() {
        every { userRepository.findById(any<Long>()) } returns Optional.of(UserCreator.getAthlete(null))
        every { activityRepository.save(any<Activity>()) } returns ActivityCreator.getDefaultActivity()

        val filePath = Paths.get("src/test/resources/fit-files/valid_file.fit").absolute()
        val name = "test.fit"
        val content = "text/plain"
        val byteContent = Files.readAllBytes(filePath)
        val resultFile = MockMultipartFile(name, name, content, byteContent)

        assertDoesNotThrow { activityService.importActivity(arrayListOf(resultFile), 1L) }
    }

    @Test
    @Transactional
    fun uploadingHealthIFitFile_shouldThrowNotImplementedError() {
        every { userRepository.findById(any<Long>()) } returns Optional.of(UserCreator.getAthlete(null))

        val filePath = Paths.get("src/test/resources/fit-files/health.fit").absolute()
        val name = "test.fit"
        val content = "text/plain"
        val byteContent = Files.readAllBytes(filePath)
        val resultFile = MockMultipartFile(name, name, content, byteContent)

        assertThrows<NotImplementedError> { activityService.importActivity(arrayListOf(resultFile), 0L) }
    }

    @Test
    @Transactional
    fun uploadingInvalidIFitFile_shouldThrowException() {
        every { userRepository.findById(any<Long>()) } returns Optional.of(UserCreator.getAthlete(null))

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
        every { userRepository.findById(any<Long>()) } returns Optional.of(UserCreator.getAthlete(null))
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

    @Test
    @WithCustomMockUser(id = -4)
    fun recogniseActivityAsPlanned_shouldReturnActivity() {
        val slot = slot<Activity>()
        every { activityRepository.save(capture(slot)) } returns ActivityCreator.getDefaultActivity()
        every { userRepository.findById(any<Long>()) } returns Optional.of(UserCreator.getAthlete(-4))
        every {
            plannedActivityRepo.findActivitiesByUserIdTypeAndDateWithoutActivity(
                any(),
                any(),
                any(),
                any(),
            )
        } returns listOf(ActivityCreator.get7Times1KmPlannedActivity(UserCreator.getAthlete(-4), UserCreator.getAthlete(-4)))


        val filePath = Paths.get("src/test/resources/fit-files/7x(1km P2').fit").absolute()
        val name = "test.fit"
        val content = "text/plain"
        val byteContent = Files.readAllBytes(filePath)
        val resultFile = MockMultipartFile(name, name, content, byteContent)

        activityService.importActivity(arrayListOf(resultFile), -4L)
        verify(exactly = 1) { plannedActivityRepo.findActivitiesByUserIdTypeAndDateWithoutActivity(any(), any(), any(), any()) }
        assertAll("Activity",
            { assertTrue(slot.captured.accuracy > 25) },
            { assertEquals(slot.captured.activityType, ActivityType.RUN) },
            { assertNotNull(slot.captured.plannedActivity) }
        )
    }

    @Test
    @WithCustomMockUser(id = -4)
    fun detectNotFittingActivity_shouldReturnNullAsAccuracy() {
        val slot = slot<Activity>()
        every { activityRepository.save(capture(slot)) } returns ActivityCreator.getDefaultActivity()
        every { userRepository.findById(any<Long>()) } returns Optional.of(UserCreator.getAthlete(-4))
        every {
            plannedActivityRepo.findActivitiesByUserIdTypeAndDateWithoutActivity(
                any(),
                any(),
                any(),
                any()
            )
        } returns listOf(ActivityCreator.get7Times1KmPlannedActivity(UserCreator.getAthlete(-4), UserCreator.getAthlete(-4)))


        val filePath = Paths.get("src/test/resources/fit-files/6x(1km P1km).fit").absolute()
        val name = "test.fit"
        val content = "text/plain"
        val byteContent = Files.readAllBytes(filePath)
        val resultFile = MockMultipartFile(name, name, content, byteContent)

        activityService.importActivity(arrayListOf(resultFile), -4L)
        verify(exactly = 1) { plannedActivityRepo.findActivitiesByUserIdTypeAndDateWithoutActivity(any(), any(), any(), any()) }
        assertAll("Activity",
            { assertEquals(slot.captured.accuracy, 0) },
            { assertEquals(slot.captured.activityType, ActivityType.RUN) },
            { assertNull(slot.captured.plannedActivity) }
        )
    }
}