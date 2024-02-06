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
import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.domain.time_constraint.pojo.dto.DailyTimeConstraintDto
import ase.athlete_view.domain.time_constraint.pojo.dto.WeeklyTimeConstraintDto
import ase.athlete_view.domain.time_constraint.pojo.entity.TimeFrame
import ase.athlete_view.domain.time_constraint.service.TimeConstraintService
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import ase.athlete_view.util.TestBase
import ase.athlete_view.util.WithCustomMockUser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
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
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@SpringBootTest(
    classes = [AthleteViewApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TimeConstraintControllerIntegrationTests: TestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    val objectMapper = ObjectMapper().registerModules(JavaTimeModule())

    @Autowired
    private lateinit var service: TimeConstraintService


    val startDateTime = LocalDateTime.of(2023,12,12,11,0)
    val endDateTime = LocalDateTime.of(2023,12,12,14,0)
    val day = DayOfWeek.MONDAY
    val userDTO = UserDTO(TEST_USER_ID, "", "", null,null, "trainer")

    @BeforeEach
    fun setupUser() {
        persistDefaultTrainer(TEST_USER_ID)
    }

    @Test
    @WithCustomMockUser(TEST_USER_ID)
    fun createDailyConstraintWithInvalidTime_shouldReturn422(){

        val constraintDto = DailyTimeConstraintDto(null, true, "", startDateTime, startDateTime)

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/constraints/dailies").with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(constraintDto))

        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andReturn().response.contentAsString

        assertTrue(result.contains("End time must be after start time"))
    }

    @Test
    @WithCustomMockUser(TEST_USER_ID)
    fun createDailyConstraint_shouldReturnSavedConstraint() {

        val constraintDto = DailyTimeConstraintDto(null, true, "test", startDateTime, endDateTime)

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/constraints/dailies").with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(constraintDto))

        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("title").value("test"))
            .andReturn().response.contentAsString

        assertTrue(result.contains("[2023,12,12,14,0]"))
    }

    @Test
    @WithCustomMockUser(TEST_USER_ID)
    fun createWeeklyConstraintWithInvalidTime_shouldReturn422(){
        val constraintDto = WeeklyTimeConstraintDto(null, true, "", TimeFrame(day, startDateTime.toLocalTime(), startDateTime.toLocalTime()))

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/constraints/weeklies").with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(constraintDto))

        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andReturn().response.contentAsString

        assertTrue(result.contains("End time must be after start time"))
    }

    @Test
    @WithCustomMockUser(TEST_USER_ID)
    fun createWeeklyConstraint_shouldReturnSavedConstraint() {

        val constraintDto = WeeklyTimeConstraintDto(null, true, "test", TimeFrame(day, startDateTime.toLocalTime(), endDateTime.toLocalTime()))

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/constraints/weeklies").with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(constraintDto))

        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("title").value("test"))
            .andReturn().response.contentAsString

        assertTrue(result.contains("weekday\":"))
    }

    @Test
    @WithCustomMockUser(TEST_USER_ID)
    fun editDailyConstraint_shouldReturnNewConstraint() {

        val constraintDto = DailyTimeConstraintDto(null, true, "", startDateTime, endDateTime)
        val savedDto = service.save(constraintDto, userDTO)
        val editDto = DailyTimeConstraintDto(savedDto.id, true, "newTitle", startDateTime, endDateTime.plusHours(2))

        val result = mockMvc.perform(
            MockMvcRequestBuilders.put("/api/constraints/dailies/$TEST_USER_ID").with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(editDto))

        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("title").value("newTitle"))
            .andReturn().response.contentAsString

        //correct end time
        assertTrue(result.contains("[2023,12,12,16,0]"))
    }

    @Test
    @WithCustomMockUser(1L)
    fun editDailyConstraintFromOtherUser_shouldReturn422() {

        val constraintDto = DailyTimeConstraintDto(null, true, "", startDateTime, endDateTime)
        val savedDto = service.save(constraintDto, userDTO)
        val editDto = DailyTimeConstraintDto(savedDto.id, true, "newTitle", startDateTime, endDateTime.plusHours(2))

        val result = mockMvc.perform(
            MockMvcRequestBuilders.put("/api/constraints/dailies/$TEST_USER_ID").with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(editDto))

        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andReturn().response.contentAsString


        assertTrue(result.contains("Cannot edit time constraint from different user"))
    }

    @Test
    @WithCustomMockUser(TEST_USER_ID)
    fun editWeeklyConstraint_shouldReturnNewConstraint() {

        val constraintDto = WeeklyTimeConstraintDto(null, true, "", TimeFrame(day, startDateTime.toLocalTime(), endDateTime.toLocalTime()))
        val savedDto = service.save(constraintDto, userDTO)
        val editDto = WeeklyTimeConstraintDto(savedDto.id, true, "newTitle", TimeFrame(day, startDateTime.toLocalTime(), endDateTime.toLocalTime().plusHours(2)))

        val result = mockMvc.perform(
            MockMvcRequestBuilders.put("/api/constraints/weeklies/$TEST_USER_ID").with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(editDto))
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("title").value("newTitle"))
            .andReturn().response.contentAsString

        //correct end time
        assertTrue(result.contains("[16,0]"))
    }

    @Test
    @WithCustomMockUser(TEST_USER_ID)
    fun editWeeklyConstraintWithInvalidTime_shouldReturn422() {

        val constraintDto = WeeklyTimeConstraintDto(null, true, "", TimeFrame(day, startDateTime.toLocalTime(), endDateTime.toLocalTime()))
        val savedDto = service.save(constraintDto, userDTO)
        val editDto = WeeklyTimeConstraintDto(savedDto.id, true, "newTitle", TimeFrame(day, startDateTime.toLocalTime(), startDateTime.toLocalTime()))

        val result = mockMvc.perform(
            MockMvcRequestBuilders.put("/api/constraints/weeklies/$TEST_USER_ID").with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(editDto))

        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andReturn().response.contentAsString

        assertTrue(result.contains("End time must be after start time"))
    }

    @Test
    @WithCustomMockUser(TEST_USER_ID)
    fun getConstraintsWithTypeDaily_shouldReturnDailies(){

        service.save(DailyTimeConstraintDto(null, true, "daily", startDateTime, endDateTime), userDTO)
        service.save(WeeklyTimeConstraintDto(null, true, "weekly", TimeFrame(day, startDateTime.toLocalTime(), endDateTime.toLocalTime())), userDTO)

        val result = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/constraints?type=daily").with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
            .andReturn().response.contentAsString

        // should work with all dates at time of testing, checks localtime at the end
        val weeklyMatch = "\"title\":\"weekly\",\"startTime\":\\[[0-9]{4},[0-9]+,[0-9]+,11,0".toRegex()

        assertAll(
            // no weeklyDto in list
            { assertTrue(!result.contains("weekday\":")) },
            // weekly constraint converted correctly
            { assertTrue(result.contains(weeklyMatch)) }
        )
    }

    @Test
    @WithCustomMockUser(TEST_USER_ID)
    fun getConstraintsWithTypeWeekly_shouldReturnWeeklies(){

        service.save(DailyTimeConstraintDto(null, true, "daily", startDateTime, endDateTime), userDTO)
        service.save(WeeklyTimeConstraintDto(null, true, "weekly", TimeFrame(day, startDateTime.toLocalTime(), endDateTime.toLocalTime())), userDTO)

        val result = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/constraints?type=weekly").with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
            .andReturn().response.contentAsString

        // should work with all dates at time of testing, checks localtime at the end
        val dailyMatch = "\"title\":\"daily\",\"startTime\":\\[[0-9]{4},[0-9]+,[0-9]+,11,0".toRegex()
        val convertTest = "\"title\":\"daily\",\"constraint\":\\{\"weekday\":\"TUESDAY\",\"startTime\":\\[11,0],".toRegex()

        assertAll(
            // weeklyDto in list
            { assertTrue(result.contains("weekday\":")) },
            // no daily constraint in list
            { assertTrue(!result.contains(dailyMatch)) },
            // daily converted correctly
            { assertTrue(result.contains(convertTest)) }
        )
    }

    @Test
    @WithCustomMockUser(TEST_USER_ID)
    fun getConstraints_shouldReturnAllConstraintsFromUser(){

        service.save(DailyTimeConstraintDto(null, true, "daily", startDateTime, endDateTime), userDTO)
        service.save(DailyTimeConstraintDto(null, true, "daily2", startDateTime.plusDays(1), endDateTime.plusDays(1)), userDTO)
        service.save(WeeklyTimeConstraintDto(null, true, "weekly", TimeFrame(day, startDateTime.toLocalTime(), endDateTime.toLocalTime())), userDTO)
        service.save(WeeklyTimeConstraintDto(null, true, "weekly2", TimeFrame(DayOfWeek.SUNDAY, startDateTime.toLocalTime(), endDateTime.toLocalTime())), userDTO)


        val result = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/constraints").with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(4))
            .andReturn().response.contentAsString

        assertAll(
            { assertTrue(result.contains("weekly\",\"constraint\":{\"weekday\":\"MONDAY\",\"startTime\":[11,0]")) },
            { assertTrue(result.contains("\"weekly2\",\"constraint\":{\"weekday\":\"SUNDAY\"")) },
            { assertTrue(result.contains("daily\",\"startTime\":[2023,12,12,11,0]")) },
            { assertTrue(result.contains("daily2\",\"startTime\":[2023,12,13,11,0]")) },
        )
    }

    @Test
    @WithCustomMockUser(1L)
    fun getConstraintsUntilBeforeConstraint_shouldReturnEmptyList() {

        service.save(DailyTimeConstraintDto(null, true, "daily", startDateTime, endDateTime), userDTO)
        service.save(WeeklyTimeConstraintDto(null, true, "weekly", TimeFrame(day, startDateTime.toLocalTime(), endDateTime.toLocalTime())), userDTO)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/constraints").with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(0))
            .andReturn().response.contentAsString
    }

    @Test
    @WithCustomMockUser(TEST_USER_ID)
    fun getConstraintsTypeDailyWithTimespan_shouldReturnWeeklyConstraintMultiple() {

        service.save(DailyTimeConstraintDto(null, true, "daily", startDateTime, endDateTime), userDTO)
        service.save(WeeklyTimeConstraintDto(null, true, "weekly", TimeFrame(day, startDateTime.toLocalTime(), endDateTime.toLocalTime())), userDTO)

        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx")
        val from = OffsetDateTime.of(2023,12,1,0,0,0,0, ZoneOffset.UTC)
        val until = OffsetDateTime.of(2023,12,31,23,59,0,0,ZoneOffset.UTC)

        val result = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/constraints?type=daily&from=${from.format(format)}&until=${until.format(format)}")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(5))
            .andReturn().response.contentAsString

        assertTrue(!result.contains("weekday\":"))
    }

    @Test
    @WithCustomMockUser(TEST_USER_ID)
    fun getConstraintsWithTimespan_shouldNotReturnDailyOutsideOfTimespan(){

        service.save(DailyTimeConstraintDto(null, true, "daily", startDateTime, endDateTime), userDTO)

        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx")
        val from = OffsetDateTime.of(2023,12,1,0,0,0,0, ZoneOffset.UTC)
        val until = OffsetDateTime.of(2023,12,7,23,59,0,0,ZoneOffset.UTC)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/constraints?type=daily&from=${from.format(format)}&until=${until.format(format)}")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(0))
    }

    @Test
    @WithCustomMockUser(TEST_USER_ID)
    fun getConstraintById_shouldReturnConstraint(){

        val constraintDto = DailyTimeConstraintDto(null, true, "test", startDateTime, endDateTime)
        val id: Long = service.save(constraintDto, userDTO).id!!

        val result = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/constraints/$id").with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("title").value("test"))
            .andReturn().response.contentAsString

        assertTrue(result.contains("[2023,12,12,14,0]"))
    }

    @Test
    @WithCustomMockUser(TEST_USER_ID)
    fun getConstraintByNonExistingId_shouldReturn404(){

        val constraintDto = DailyTimeConstraintDto(null, true, "test", startDateTime, endDateTime)
        val id: Long = service.save(constraintDto, userDTO).id!!

        val result = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/constraints/${id+1}").with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andReturn().response.contentAsString

        assertTrue(result.contains("Could not find constraint by given id"))
    }

    @Test
    @WithCustomMockUser(TEST_USER_ID)
    fun deleteConstraint_shouldRemoveConstraintFromDB(){

        val constraintDto = DailyTimeConstraintDto(null, true, "test", startDateTime, endDateTime)
        val id: Long = service.save(constraintDto, userDTO).id!!

        assertDoesNotThrow { service.getById(id, userDTO) }

        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/constraints/${id}").with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk)

        assertThrows<NotFoundException> { service.getById(id, userDTO) }
    }

    @Test
    @WithCustomMockUser(1L)
    fun deleteConstraintFromOtherUser_shouldReturn422(){

        val constraintDto = DailyTimeConstraintDto(null, true, "test", startDateTime, endDateTime)
        val id: Long = service.save(constraintDto, userDTO).id!!

        val result = mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/constraints/${id}").with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andReturn().response.contentAsString

        assertTrue(result.contains("Cannot delete time constraint from different user"))
    }
}
