package ase.athlete_view.unit

import ase.athlete_view.AthleteViewApplication
import ase.athlete_view.common.exception.entity.NoMapDataException
import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.domain.activity.persistence.ActivityRepository
import ase.athlete_view.domain.activity.persistence.FitDataRepositoryImpl
import ase.athlete_view.domain.activity.persistence.PlannedActivityRepository
import ase.athlete_view.domain.activity.pojo.dto.FitData
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.user.persistence.UserRepository
import ase.athlete_view.util.ActivityCreator
import ase.athlete_view.util.UserCreator
import ase.athlete_view.util.WithCustomMockUser
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayInputStream
import java.time.LocalDateTime
import java.util.*

@SpringBootTest(classes = [AthleteViewApplication::class])
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
class ActivityServiceMapAndStatisticsUnitTests {
    @Autowired
    private lateinit var activityService: ActivityService

    @MockkBean
    private lateinit var plannedActivityRepo: PlannedActivityRepository

    @MockkBean
    private lateinit var userRepository: UserRepository

    @MockkBean
    private lateinit var activityRepository: ActivityRepository

    @MockkBean
    private lateinit var fitRepository: FitDataRepositoryImpl // for... reasons, cannot specify interface here


    @Test
    @WithCustomMockUser(id = -2)
    fun fetchingMapDataOfNonEmptyActivity_shouldSucceed() {
        val mockUserObj = UserCreator.getAthlete(-2)
        val fileBytes = this::class.java.classLoader.getResource("fit-files/7x(1km P2').fit")?.readBytes()

        val act = ActivityCreator.getDefaultActivity()
        act.user = mockUserObj
        act.fitData = "totallyfitdata"

        every { userRepository.findById(any<Long>()) } returns Optional.of(mockUserObj)
        every { activityRepository.findById(any<Long>()) } returns Optional.of(act)
        every { activityRepository.findActivitiesByUserId(any<Long>()) } returns listOf(act)
        every { fitRepository.getFitData(any<String>()) } returns FitData("totallyfitdata", ByteArrayInputStream(fileBytes))

        val mapData = activityService.prepareMapDataForActivity(-2, 1)
        assertThat(mapData).isNotNull
        assertThat(mapData.size).isGreaterThan(0)
        assertThat(mapData).noneMatch { it.latitude == 0.0 && it.longitude == 0.0 }
    }


    @Test
    @WithCustomMockUser(id = -2)
    fun fetchingMapDataOfEmptyActivity_shouldThrowException() {
        val mockUserObj = UserCreator.getAthlete(-2)
        val act = ActivityCreator.getDefaultActivity()
        act.user = mockUserObj
        act.fitData = null

        every { userRepository.findById(any<Long>()) } returns Optional.of(mockUserObj)
        every { activityRepository.findById(any<Long>()) } returns Optional.of(act)
        every { activityRepository.findActivitiesByUserId(any<Long>()) } returns listOf(act)

        assertThrows<NoMapDataException> { activityService.prepareMapDataForActivity(-2, 1) }
    }

    @Test
    @WithCustomMockUser(id = -2)
    fun fetchingMapDataOfActivityOfOtherUser_shouldThrowException() {
        val mockUserObj = UserCreator.getAthlete(-2)
        val act = ActivityCreator.getDefaultActivity()
        act.user = UserCreator.getTrainer()
        act.fitData = null

        every { userRepository.findById(any<Long>()) } returns Optional.of(mockUserObj)
        every { activityRepository.findById(any<Long>()) } returns Optional.of(act)
        every { activityRepository.findActivitiesByUserId(any<Long>()) } returns listOf(act)

        assertThrows<NoMapDataException> { activityService.prepareMapDataForActivity(-2, 1) }
    }

    @Test
    @WithCustomMockUser(id = -2)
    fun fetchingMapDataOfEmptyActivity_shouldSucceed() {
        val mockUserObj = UserCreator.getAthlete(-2)
        val act = ActivityCreator.getDefaultActivity()
        act.user = mockUserObj
        act.fitData = "asdf123"
        val fileBytes = this::class.java.classLoader.getResource(("fit-files/no-coords.fit"))?.readBytes()

        every { userRepository.findById(any<Long>()) } returns Optional.of(mockUserObj)
        every { activityRepository.findById(any<Long>()) } returns Optional.of(act)
        every { activityRepository.findActivitiesByUserId(any<Long>()) } returns listOf(act)
        every { fitRepository.getFitData(any<String>()) } returns FitData("asdf123", ByteArrayInputStream(fileBytes))

        val coords = activityService.prepareMapDataForActivity(-2, 1)
        assertThat(coords).isNotNull
        assertThat(coords.size).isZero()
    }

    @Test
    @WithCustomMockUser(id = -3)
    fun fetchingMapDataFromAthleteActivityAsTrainer_shouldSucceed() {
        val mockUserObj = UserCreator.getAthlete(-2)
        val act = ActivityCreator.getDefaultActivity()
        act.user = mockUserObj
        act.fitData = "asdf123"
        val fileBytes = this::class.java.classLoader.getResource(("fit-files/no-coords.fit"))?.readBytes()

        val mockTrainer = UserCreator.getTrainer()
        mockTrainer.id = -3
        mockTrainer.athletes.add(mockUserObj)

        every { userRepository.findById(any<Long>()) } returns Optional.of(mockTrainer)
        every { activityRepository.findById(any<Long>()) } returns Optional.of(act)
        every { activityRepository.findActivitiesByUserId(any<Long>()) } returns listOf(act)
        every { fitRepository.getFitData(any<String>()) } returns FitData("asdf123", ByteArrayInputStream(fileBytes))

        val coords = activityService.prepareMapDataForActivity(-3, 1)
        assertThat(coords).isNotNull
        assertThat(coords.size).isZero()
    }

    @Test
    @Transactional
    @WithCustomMockUser(id = -2)
    fun fetchingStatisticsOfNonEmptyActivity_shouldSucceed() {
        val mockUserObj = UserCreator.getAthlete(-2)
        val fileBytes = this::class.java.classLoader.getResource(("fit-files/7x(1km P2').fit"))?.readBytes()
        val act = ActivityCreator.getDefaultActivity()
        act.user = mockUserObj
        act.fitData = "totallyfitdata"

        every { userRepository.findById(any<Long>()) } returns Optional.of(mockUserObj)
        every { activityRepository.findById(any<Long>()) } returns Optional.of(act)
        every { activityRepository.findActivitiesByUserId(any<Long>()) } returns listOf(act)
        every { fitRepository.getFitData(any<String>()) } returns FitData("totallyfitdata", ByteArrayInputStream(fileBytes))

        val stats = activityService.prepareStatisticsForActivity(-2, 1)
        assertThat(stats).isNotNull
        assertThat(stats.size).isGreaterThan(0)
        assertThat(stats).noneMatch { it.speed == 0.0f && it.cadence == 0.toShort() && it.power == 0 && it.bpm == 0 }

        // verify no duplicates (by timestamp)
        val timeSet = mutableSetOf<LocalDateTime>()
        stats.forEach { timeSet.add(it.timestamp) }
        assertThat(stats.size).isEqualTo(timeSet.size)
    }

    @Test
    @WithCustomMockUser(id = -2)
    fun fetchStatisticsOfActivityWithoutFitData_shouldReturnEmptyList() {
        val mockUserObj = UserCreator.getAthlete(-2)
        val act = ActivityCreator.getDefaultActivity()
        act.user = mockUserObj
        act.fitData = null

        every { userRepository.findById(any<Long>()) } returns Optional.of(mockUserObj)
        every { activityRepository.findById(any<Long>()) } returns Optional.of(act)
        every { activityRepository.findActivitiesByUserId(any<Long>()) } returns listOf(act)

        val data = activityService.prepareStatisticsForActivity(-2, 1)
        assertThat(data).isNotNull
        assertThat(data.size).isZero()
    }

    @Test
    @WithCustomMockUser(id = -2)
    fun attemptToFetchStatisticsOfActivityOfAnotherUser_shouldThrowException() {
        val mockUserObj = UserCreator.getAthlete(-2)

        val act = ActivityCreator.getDefaultActivity()
        act.id = 10
        act.user = UserCreator.getTrainer()
        act.fitData = "test213"

        every { userRepository.findById(any<Long>()) } returns Optional.of(mockUserObj)
        every { activityRepository.findById(any<Long>()) } returns Optional.of(act)
        every { activityRepository.findActivitiesByUserId(any<Long>()) } returns listOf(ActivityCreator.getDefaultActivity())

        assertThrows<NotFoundException> { activityService.prepareStatisticsForActivity(-2, 10) }
    }

    @Test
    @WithCustomMockUser(id = -2)
    fun attemptToFetchStatisticsOfInvalidActivity_shouldThrowException() {
        val mockUserObj = UserCreator.getAthlete(-2)

        val act = ActivityCreator.getDefaultActivity()
        act.user = mockUserObj
        act.fitData = "test213"

        every { userRepository.findById(any<Long>()) } returns Optional.of(mockUserObj)
        every { activityRepository.findById(any<Long>()) } returns Optional.empty()

        assertThrows<NotFoundException> { activityService.prepareStatisticsForActivity(-2, 1) }
    }

    @Test
    @WithCustomMockUser(id = -2)
    fun attemptToFetchStatisticsOfInvalidUser_shouldThrowException() {
        val mockUserObj = UserCreator.getAthlete(-2)

        val act = ActivityCreator.getDefaultActivity()
        act.user = mockUserObj
        act.fitData = "test213"

        every { userRepository.findById(any<Long>()) } returns Optional.empty()

        assertThrows<NotFoundException> { activityService.prepareStatisticsForActivity(-2, 1) }
    }

    @Test
    @WithCustomMockUser(id = -3)
    fun fetchStatisticsForAthleteActivityAsTrainer_shouldSucceed() {
        val mockUserObj = UserCreator.getAthlete(-2)
        val act = ActivityCreator.getDefaultActivity()
        act.user = mockUserObj
        act.fitData = null

        val trainer = UserCreator.getTrainer()
        trainer.id = -3
        trainer.athletes = mutableSetOf(mockUserObj)

        every { userRepository.findById(any<Long>()) } returns Optional.of(trainer)
        every { activityRepository.findById(any<Long>()) } returns Optional.of(act)
        every { activityRepository.findActivitiesByUserId(any<Long>()) } returns listOf(act)

        val data = activityService.prepareStatisticsForActivity(trainer.id!!, act.id!!)
        assertThat(data).isNotNull
        assertThat(data.size).isZero()
    }
}