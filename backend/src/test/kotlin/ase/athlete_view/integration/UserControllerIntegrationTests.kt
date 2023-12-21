package ase.athlete_view.integration

import ase.athlete_view.AthleteViewApplication
import ase.athlete_view.domain.user.controller.UserController
import ase.athlete_view.domain.user.service.UserService
import ase.athlete_view.domain.user.service.mapper.UserMapper
import ase.athlete_view.util.TestBase
import ase.athlete_view.util.WithCustomMockUser
import io.github.oshai.kotlinlogging.KotlinLogging
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*


@SpringBootTest(
    classes=[AthleteViewApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTests: TestBase(){
    val log = KotlinLogging.logger {}

    @Autowired
    private lateinit var userController: UserController

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var athleteService: UserService

    @Autowired
    private lateinit var userMapper: UserMapper

    @Autowired
    private lateinit var mockMvc: MockMvc


    @Test
    @WithCustomMockUser(id=-3)
    fun getAllAthletesByTrainerId_shouldReturnListOfAthletes() {
        mockMvc.perform(
            get("/api/user/athlete").with(csrf())
                .contentType("application/json")
                .characterEncoding("utf-8")
        ).andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize<Any>(1)))
            .andExpect(jsonPath("$[0].id").value(-2))
    }

    @Test
    @WithCustomMockUser(id=-2)
    fun getAllAthletesByTrainerIdWithAthleteId_shouldReturnForbidden() {
        mockMvc.perform(
            get("/api/user/athlete").with(csrf())
                .contentType("application/json")
                .characterEncoding("utf-8")
        ).andExpect(status().isForbidden())

    }
}
