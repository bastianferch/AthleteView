package ase.athlete_view.integration.fitness

import ase.athlete_view.domain.activity.persistence.ActivityRepository
import ase.athlete_view.domain.activity.pojo.entity.Activity
import ase.athlete_view.domain.health.persistence.HealthRepository
import ase.athlete_view.domain.health.pojo.entity.Health
import ase.athlete_view.domain.user.service.UserService
import ase.athlete_view.util.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.time.LocalDate

const val TEST_ATHLETE_ID = -1L
const val TEST_TRAINER_ID = -3L
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FitnessControllerIntegrationTests : TestBase() {
    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var activityRepository: ActivityRepository

    @Autowired
    private lateinit var healthRepository: HealthRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var webContext: WebApplicationContext
    
    @BeforeEach
    fun mvcSetup() {
        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(this.webContext)
            .apply<DefaultMockMvcBuilder?>(SecurityMockMvcConfigurers.springSecurity())
            .build()
    }

    @Test
    @WithCustomMockUser(TEST_ATHLETE_ID)
    @DisplayName("(+) Calculate the perfect fitness when only one day is persisted")
    fun calculateFitnessProvidesCorrectData() {
        val user = this.userService.getById(TEST_ATHLETE_ID)
        val yesterday = LocalDate.now().minusDays(1)
        val activity = ActivityCreator.getHealthyForDefaultAthleteActivity(user = user, date = yesterday)
        activityRepository.save(activity)

        val health = HealthCreator.getDefaultGoodHealth(
            user = user,
            date = yesterday
        )
        this.healthRepository.save(health)
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/fitness").with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .param("targetUserId", TEST_ATHLETE_ID.toString())
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(5))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0]").value(100))
    }

    @Test
    @WithCustomMockUser(TEST_ATHLETE_ID)
    @DisplayName("(+) Calculate the perfect fitness when all week is persisted")
    fun calculatePerfectFitnessWhenAllWeakIsPersisted() {
        val user = this.userService.getById(TEST_ATHLETE_ID)
        val healthList = ArrayList<Health>()
        val activityList = ArrayList<Activity>()
        val today = LocalDate.now()
        var dayIterator = today.minusDays(7)
        while (!dayIterator.isEqual(today)) {
            val activity = ActivityCreator.getHealthyForDefaultAthleteActivity(user = user, date = dayIterator)
            activityList.add(activity)

            val health = HealthCreator.getDefaultGoodHealth(
                user = user,
                date = dayIterator
            )
            healthList.add(health)

            dayIterator = dayIterator.plusDays(1)
        }
        this.activityRepository.saveAll(activityList)
        this.healthRepository.saveAll(healthList)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/fitness").with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .param("targetUserId", TEST_ATHLETE_ID.toString())
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(5))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0]").value(100))
    }

    @Test
    @WithCustomMockUser(TEST_ATHLETE_ID)
    @DisplayName("(+) Calculate the perfect fitness when two weeks are persisted")
    fun calculatePerfectFitnessWhenTwoWeeksArePersisted() {
        val user = this.userService.getById(TEST_ATHLETE_ID)
        val healthList = ArrayList<Health>()
        val activityList = ArrayList<Activity>()
        val today = LocalDate.now()
        var dayIterator = today.minusDays(14)
        val weekAgo = today.minusDays(7)
        while (!dayIterator.isEqual(today)) {
            if (dayIterator.isAfter(weekAgo)) {
                activityList.add(
                    ActivityCreator.getHealthyForDefaultAthleteActivity(user = user, date = dayIterator)
                )
                healthList.add(
                    HealthCreator.getDefaultGoodHealth(
                        user = user,
                        date = dayIterator
                    )
                )
            } else {
                activityList.add(
                    ActivityCreator.getMehForDefaultAthleteActivity(user = user, date = dayIterator)
                )
                healthList.add(
                    HealthCreator.getDefaultMehHealth(
                        user = user,
                        date = dayIterator
                    )
                )
            }
            dayIterator = dayIterator.plusDays(1)
        }
        this.activityRepository.saveAll(activityList)
        this.healthRepository.saveAll(healthList)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/fitness").with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .param("targetUserId", TEST_ATHLETE_ID.toString())
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(5))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0]").value(100))
    }

    @Test
    @WithCustomMockUser(TEST_ATHLETE_ID)
    @DisplayName("(+) Calculate the fitness when only health is persisted")
    fun calculateFitnessWithOnlyHealthPersisted() {
        val user = this.userService.getById(TEST_ATHLETE_ID)
        val yesterday = LocalDate.now().minusDays(1)

        val health = HealthCreator.getDefaultGoodHealth(
            user = user,
            date = yesterday
        )
        this.healthRepository.save(health)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/fitness").with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .param("targetUserId", TEST_ATHLETE_ID.toString())
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(5))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0]").value(70))
    }

    @Test
    @WithCustomMockUser(TEST_ATHLETE_ID)
    @DisplayName("(+) Calculate the fitness when only activity is persisted")
    fun calculateFitnessWithOnlyActivityPersisted() {
        val user = this.userService.getById(TEST_ATHLETE_ID)
        val yesterday = LocalDate.now().minusDays(1)
        val activity = ActivityCreator.getHealthyForDefaultAthleteActivity(user = user, date = yesterday)
        activityRepository.save(activity)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/fitness").with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .param("targetUserId", TEST_ATHLETE_ID.toString())
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(5))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0]").value(-1))
    }

    @Test
    @WithCustomMockUser(TEST_ATHLETE_ID)
    @DisplayName("(+) Calculate the fitness when only one day is persisted and activity is meh")
    fun calculateFitnessWithLowActivityAndLowHealth() {
        val user = this.userService.getById(TEST_ATHLETE_ID)
        val yesterday = LocalDate.now().minusDays(1)
        val activity = ActivityCreator.getDefaultActivity()
        activity.user = user
        activity.startTime = yesterday.atTime(15, 0, 0)
        activity.endTime = yesterday.atTime(16, 0, 0)
        activity.averageBpm = 80
        activity.maxBpm = 110
        activityRepository.save(activity)

        val health = HealthCreator.getDefaultGoodHealth(
            user = user,
            date = yesterday
        )
        this.healthRepository.save(health)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/fitness").with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .param("targetUserId", TEST_ATHLETE_ID.toString())
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(5))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0]").value(94))
    }

    @Test
    @WithCustomMockUser(TEST_ATHLETE_ID)
    @DisplayName("(+) Calculate the fitness when only one day is persisted and there are 2 activities")
    fun calculateFitnessWithOneHealthAndTwoActivitiesAtOneDay() {
        val user = this.userService.getById(TEST_ATHLETE_ID)
        val yesterday = LocalDate.now().minusDays(1)
        val activity1 = ActivityCreator.getDefaultActivity()
        activity1.user = user
        activity1.id = null
        activity1.startTime = yesterday.atTime(15, 0, 0)
        activity1.endTime = yesterday.atTime(16, 0, 0)
        activity1.averageBpm = 80
        activity1.maxBpm = 110
        val activity2 = ActivityCreator.getDefaultActivity()
        activity2.user = user
        activity2.id = null
        activity2.startTime = yesterday.atTime(17, 0, 0)
        activity2.endTime = yesterday.atTime(18, 0, 0)
        activity2.averageBpm = 100
        activity2.maxBpm = 190
        activityRepository.saveAll(listOf(activity1, activity2))

        val health = HealthCreator.getDefaultGoodHealth(
            user = user,
            date = yesterday
        )
        this.healthRepository.save(health)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/fitness").with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .param("targetUserId", TEST_ATHLETE_ID.toString())
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(5))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0]").value(94))
    }

    @Test
    @WithCustomMockUser(TEST_ATHLETE_ID)
    @DisplayName("(+) Calculate the fitness when only one day is persisted and both activity and health is meh")
    fun calculateFitnessWithLowActivity() {
        val user = this.userService.getById(TEST_ATHLETE_ID)
        val yesterday = LocalDate.now().minusDays(1)
        val activity = ActivityCreator.getDefaultActivity()
        activity.user = user
        activity.startTime = yesterday.atTime(15, 0, 0)
        activity.endTime = yesterday.atTime(16, 0, 0)
        activity.averageBpm = 80
        activity.maxBpm = 110
        activityRepository.save(activity)

        val health = HealthCreator.getDefaultMehHealth(
            user = user,
            date = yesterday
        )
        this.healthRepository.save(health)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/fitness").with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .param("targetUserId", TEST_ATHLETE_ID.toString())
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(5))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0]").value(53))
    }

    @Test
    @WithCustomMockUser(TEST_TRAINER_ID)
    @DisplayName("(-) Calculate fitness for the trainer")
    fun calculateFitnessForTrainer() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/fitness").with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .param("targetUserId", TEST_ATHLETE_ID.toString())
        )
            .andExpect(MockMvcResultMatchers.status().is4xxClientError())
    }

    @Test
    @WithCustomMockUser(TEST_ATHLETE_ID)
    @DisplayName("(+) Calculate fitness when no data for the last week is provided")
    fun calculateFitnessWhenNoDataIsPersisted() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/fitness").with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .param("targetUserId", TEST_ATHLETE_ID.toString())
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(5))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0]").value(-1))
    }
}