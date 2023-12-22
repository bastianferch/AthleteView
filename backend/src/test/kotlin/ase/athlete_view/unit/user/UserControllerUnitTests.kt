package ase.athlete_view.unit.user

import ase.athlete_view.AthleteViewApplication
import ase.athlete_view.config.SecurityConfig
import ase.athlete_view.config.jwt.UserAuthProvider
import ase.athlete_view.domain.authentication.controller.AuthenticationController
import ase.athlete_view.domain.authentication.service.AuthenticationService
import ase.athlete_view.domain.user.controller.UserController
import ase.athlete_view.domain.user.service.AthleteService
import ase.athlete_view.domain.user.service.UserService
import ase.athlete_view.domain.user.service.mapper.UserMapper
import ase.athlete_view.util.UserCreator
import ase.athlete_view.util.WithCustomMockUser
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@WebMvcTest(controllers = [UserController::class, AuthenticationController::class])
@ContextConfiguration(classes = [SecurityConfig::class, AthleteViewApplication::class])
@ActiveProfiles("test")
class UserControllerUnitTests {

    @Autowired
    private lateinit var webContext: WebApplicationContext

    @MockkBean
    private lateinit var athleteService: AthleteService

    @MockkBean
    lateinit var authService: AuthenticationService

    @MockkBean
    lateinit var authProvider: UserAuthProvider

    @MockkBean
    private lateinit var userMapper: UserMapper

    @MockkBean
    private lateinit var userService: UserService

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun mvcSetup() {
        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(this.webContext)
            .apply<DefaultMockMvcBuilder?>(SecurityMockMvcConfigurers.springSecurity())
            .build()
    }

    @Test
    @WithCustomMockUser(id=-1)
    fun getUsersByTrainerId_shouldReturnAthleteDTOList() {
        every {athleteService.getByTrainerId(any<Long>())} returns listOf(UserCreator.getAthlete(1), UserCreator.getAthlete(2), UserCreator.getAthlete(3))
        mockMvc.perform(get("/api/user/athlete"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Any>(3)))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[2].id").value(3))
    }
}
