package ase.athlete_view.unit

import ase.athlete_view.config.SecurityConfig
import ase.athlete_view.config.jwt.UserAuthProvider
import ase.athlete_view.domain.authentication.controller.AuthenticationController
import ase.athlete_view.domain.authentication.dto.LoginDTO
import ase.athlete_view.domain.authentication.service.AuthenticationService
import ase.athlete_view.domain.user.pojo.dto.UserDto
import ase.athlete_view.domain.user.service.mapper.UserMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*


@WebMvcTest(AuthenticationController::class)
//@ContextConfiguration(classes=[SecurityConfig::class])
class AuthenticationControllerUnitTests {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean // required cuz injected but not used right now
    private lateinit var userMapper: UserMapper

    @MockkBean
    lateinit var authService: AuthenticationService

    @MockkBean
    lateinit var authProvider: UserAuthProvider

    private val user = UserDto(1, "a@b.com", "Max Mustermann", "musterpassword", "sampletokendefinvalid")
    private val login = LoginDTO("a@b.com", "musterpassword")

    @Test
    fun givenExistingUser_LoginReturnsOk() {
        every { authService.authenticateUser(login) } returns user

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(login.toString())
                .with(csrf())
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
    }
}