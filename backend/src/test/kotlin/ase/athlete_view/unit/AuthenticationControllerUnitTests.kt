package ase.athlete_view.unit

import ase.athlete_view.AthleteViewApplication
import ase.athlete_view.config.SecurityConfig
import ase.athlete_view.config.jwt.UserAuthProvider
import ase.athlete_view.domain.authentication.controller.AuthenticationController
import ase.athlete_view.domain.authentication.dto.LoginDTO
import ase.athlete_view.domain.authentication.service.AuthenticationService
import ase.athlete_view.domain.user.pojo.dto.UserDto
import ase.athlete_view.domain.user.service.mapper.UserMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext


@WebMvcTest(controllers = [AuthenticationController::class])
@ContextConfiguration(classes = [SecurityConfig::class, AthleteViewApplication::class])
class AuthenticationControllerUnitTests {
    @Autowired
    private lateinit var webContext: WebApplicationContext

    @MockkBean
    lateinit var authService: AuthenticationService

    // required cuz injected
    @MockkBean
    private lateinit var userMapper: UserMapper

    @MockkBean
    lateinit var authProvider: UserAuthProvider

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(this.webContext)
            .apply<DefaultMockMvcBuilder?>(springSecurity())
            .build()
    }

    @Test
    fun givenExistingUser_LoginReturnsOk() {
        val user = UserDto(1, "a@b.com", "Max Mustermann", "musterpassword", "sampletokendefinvalid")
        val login = LoginDTO("a@b.com", "musterpassword")


        every { authService.authenticateUser(any<LoginDTO>()) } returns user

        mockMvc.perform(
            post("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(login.toJsonString())
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
        verify(exactly = 1) { authService.authenticateUser(login) }
    }
}