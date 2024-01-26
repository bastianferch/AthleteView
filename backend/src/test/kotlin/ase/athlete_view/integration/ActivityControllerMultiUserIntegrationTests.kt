package ase.athlete_view.integration

import ase.athlete_view.AthleteViewApplication
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.util.WithCustomMockUser
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(
        classes = [AthleteViewApplication::class],
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ActivityControllerMultiUserIntegrationTests {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var activityService: ActivityService

    @BeforeEach
    fun importActivityAsAthlete() {
        val byteContent = this::class.java.classLoader.getResource("fit-files/7x(1km P2').fit")?.readBytes()
        val name = "test.fit"
        val resultFile = MockMultipartFile("files", name, MediaType.MULTIPART_FORM_DATA_VALUE, byteContent)

        activityService.importActivity(listOf(resultFile), -2)
    }

    @Test
    @WithCustomMockUser(id = -3)
    @Transactional
    fun fetchMapDataForValidAthleteActivityAsTrainer_shouldSucceed() {
        mockMvc.perform(
                get("/api/activity/map/1")
        )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.length()").value(3104))
    }

    @Test
    @WithCustomMockUser(id = -3)
    @Transactional
    fun fetchActivityStatisticsForAthleteActivityAsTrainer_shouldSucceed() {
        mockMvc.perform(
                get("/api/activity/statistics/1")
        )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isNotEmpty)
                .andExpect(jsonPath("$.length()").value(3104))
    }

    @Test
    @WithCustomMockUser(id = -2)
    @Transactional
    fun fetchActivityStatisticsAsUser_shouldSucceed() {
        mockMvc.perform(
                get("/api/activity/statistics/1")
        )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isNotEmpty)
                .andExpect(jsonPath("$.length()").value(3104))
    }

    @Test
    @WithCustomMockUser(id = -2)
    @Transactional
    fun fetchStatisticsForInvalidIdAsAthlete_shouldReturnNotFound() {
        mockMvc.perform(
                get("/api/activity/statistics/999")
        )
                .andExpect(status().isNotFound)
    }

    @Test
    @WithCustomMockUser(id = -1)
    @Transactional
    fun fetchStatisticsForValidActivityFromInvalidAthlete_shouldReturnNotFound() {
        mockMvc.perform(
                get("/api/activity/statistics/1")
        )
                .andExpect(status().isNotFound)
    }

    @Test
    @WithCustomMockUser(id = -1)
    @Transactional
    fun fetchMapDataForValidActivityFromInvalidUser_shouldReturnNotFound() {
        mockMvc.perform(
                get("/api/activity/map/1")
        )
                .andExpect(status().isNotFound)
    }
}