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
