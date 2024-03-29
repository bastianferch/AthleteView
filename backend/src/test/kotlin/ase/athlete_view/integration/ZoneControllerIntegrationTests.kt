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
package ase.athlete_view.integration

import ase.athlete_view.AthleteViewApplication
import ase.athlete_view.domain.user.persistence.AthleteRepository
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.zone.service.ZoneService
import ase.athlete_view.util.TestBase
import ase.athlete_view.util.UserCreator
import ase.athlete_view.util.WithCustomMockUser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest(
    classes = [AthleteViewApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ZoneControllerIntegrationTests: TestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    val objectMapper = ObjectMapper().registerModules(JavaTimeModule())

    @Autowired
    private lateinit var zoneService: ZoneService

    @Autowired
    private lateinit var athleteRepository: AthleteRepository

    @Test
    @WithCustomMockUser(id = TEST_USER_ID)
    fun resetZones_shouldCreateZonesAndReturnOk() {

        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/zones").with(SecurityMockMvcRequestPostProcessors.csrf())

        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(5))
            .andReturn().response.contentAsString
    }

    @Test
    @WithCustomMockUser(id = TEST_USER_ID)
    fun resetZonesWithCustomMaxHR_shouldCreateZonesAndReturnOk() {

        val result = mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/zones?maxHR=212").with(SecurityMockMvcRequestPostProcessors.csrf())

        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(5))
            .andReturn().response.contentAsString

        assertTrue(result.contains("\"toBPM\":212}]"))
    }

    @Test
    @WithCustomMockUser(TEST_USER_ID)
    fun putZonesWithValidZones_shouldReturn201AndValidZones() {

        val zones = zoneService.calcZones(212)
        zones[4].toBPM = 215
        val start = "\"fromBPM\":0".toRegex()
        val end = "\"toBPM\":215}]".toRegex()

        val result = mockMvc.perform(
            MockMvcRequestBuilders.put("/api/zones").with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(zones))
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(5))
            .andReturn().response.contentAsString

        assertAll(
            { assertTrue(start.containsMatchIn(result)) },
            { assertTrue(end.containsMatchIn(result)) }
        )
    }

    @Test
    @WithCustomMockUser(TEST_USER_ID)
    fun putZonesWithInvalidZones_shouldReturn422() {
        val zones = zoneService.calcZones(212)
        zones[3].toBPM = 187

        val result = mockMvc.perform(
            MockMvcRequestBuilders.put("/api/zones").with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(zones))
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andReturn().response.contentAsString

        assertTrue(result.contains("Zones not covering full range"))
    }

    @Test
    @WithCustomMockUser(1)
    fun getZones_returnsCorrectZonesForUser(){

        val resultMatch = "\"toBPM\":18[6789]}]".toRegex()
        val athlete: Athlete = UserCreator.getAthlete(1)
        athlete.trainer = null
        athleteRepository.save(athlete)
        zoneService.resetZones(1, null)

        val result = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/zones").with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(5))
            .andReturn().response.contentAsString

        assertTrue(resultMatch.containsMatchIn(result))
    }

    @Test
    @WithCustomMockUser(TEST_USER_ID)
    fun getZonesFromNotExisting_shouldReturnEmpty(){

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/zones").with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(0))
            .andReturn().response.contentAsString
    }

}
