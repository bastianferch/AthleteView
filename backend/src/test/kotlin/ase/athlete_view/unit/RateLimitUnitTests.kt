/*-
 * #%L
 * athlete_view
 * %%
 * Copyright (C) 2023 - 2024 TU Wien INSO ASE GROUP 5 WS2023
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
package ase.athlete_view.unit

import ase.athlete_view.AthleteViewApplication
import ase.athlete_view.config.SecurityConfig
import ase.athlete_view.config.jwt.UserAuthProvider
import ase.athlete_view.config.rate_limit.RateLimitConfig
import ase.athlete_view.domain.authentication.controller.AuthenticationController
import ase.athlete_view.domain.authentication.dto.LoginDTO
import ase.athlete_view.domain.authentication.service.AuthService
import ase.athlete_view.util.UserCreator
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@WebMvcTest(controllers = [AuthenticationController::class])
@ContextConfiguration(classes = [SecurityConfig::class, RateLimitConfig::class, AthleteViewApplication::class])
@ActiveProfiles("test", "rateLimitTest")
class RateLimitUnitTests {


    @Autowired
    private lateinit var webContext: WebApplicationContext

    @MockkBean
    lateinit var authService: AuthService

    @MockkBean
    lateinit var authProvider: UserAuthProvider

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper


    @BeforeEach
    fun mvcSetup() {
        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(this.webContext)
            .apply<DefaultMockMvcBuilder?>(SecurityMockMvcConfigurers.springSecurity())
            .build()
    }



    @Test
    fun sendNewConfirmationLinkTwiceWithinShortTime_Returns429(){
        every { authService.sendNewConfirmationToken(any()) } just runs
        val dto = LoginDTO(UserCreator.DEFAULT_ATHLETE_EMAIL, password = UserCreator.DEFAULT_ATHLETE_PASSWORD)

        // first time succeeds
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/confirmation/new")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        )
            .andExpect(MockMvcResultMatchers.status().isNoContent)

        // second time: too many requests
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/confirmation/new")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        )
            .andExpect(MockMvcResultMatchers.status().isTooManyRequests)
    }


    @Test
    fun failToLogInSixTimes_shouldSuspendAccount(){
        //login fails
        every { authService.authenticateUser(any<LoginDTO>()) } throws BadCredentialsException("")

        val login = UserCreator.getAthleteLoginDto()

        // fail 5 times
        for (i in 0..4) {
            mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/login")
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(objectMapper.writeValueAsString(login))
            )
                .andExpect(MockMvcResultMatchers.status().isForbidden)
        }


        // sixth time runs into rate limit, even when login should succeed
        every { authService.authenticateUser(any<LoginDTO>()) } returns UserCreator.getAthleteDTO()
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/login")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(login))
        )
            .andExpect(MockMvcResultMatchers.status().isTooManyRequests)
    }
}
