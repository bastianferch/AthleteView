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

import ase.athlete_view.domain.time_constraint.persistence.TimeConstraintRepository
import ase.athlete_view.domain.time_constraint.persistence.WeeklyTimeConstraintRepository
import ase.athlete_view.domain.time_constraint.pojo.dto.TimeConstraintDto
import ase.athlete_view.domain.time_constraint.pojo.dto.WeeklyTimeConstraintDto
import ase.athlete_view.domain.time_constraint.pojo.dto.DailyTimeConstraintDto
import ase.athlete_view.domain.time_constraint.pojo.entity.TimeConstraint
import ase.athlete_view.domain.time_constraint.pojo.entity.TimeFrame
import ase.athlete_view.domain.time_constraint.pojo.entity.WeeklyTimeConstraint
import ase.athlete_view.domain.time_constraint.service.TimeConstraintService
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import org.junit.jupiter.api.Assertions.assertEquals
import ase.athlete_view.domain.user.pojo.entity.User
import ase.athlete_view.domain.user.service.UserService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

@SpringBootTest
@ActiveProfiles("test")
class TimeConstraintServiceUnitTests {

    @MockkBean
    private lateinit var repo: TimeConstraintRepository

    @MockkBean
    private lateinit var weeklyRepo: WeeklyTimeConstraintRepository

    @MockkBean
    private lateinit var us: UserService

    @Autowired
    private lateinit var service: TimeConstraintService

    private val user = User(1, "a@s.com", listOf(), "Josef", "asdf", "Austria", "1337")
    private val userDto = UserDTO(1, "","",null,null, "trainer")
    private val constraint = TimeConstraint(1, true, "TestConstraint", user)
    private val newConstraintDto = TimeConstraintDto(null, true, "TestConstraint")
    private val weeklyConstraint = WeeklyTimeConstraint(2, true, "TestConstraint", user, TimeFrame(DayOfWeek.MONDAY, LocalTime.of(11,0), LocalTime.of(12,0)))

    @Test
    fun saveConstraint(){
        every { repo.save(any()) } returns constraint
        every { us.getById(1) } returns user

        val saved: TimeConstraintDto = service.save(newConstraintDto, userDto)
        assertEquals(saved.id, constraint.id)
    }

    @Test
    fun saveInvalidWeeklyConstraint(){
        every { us.getById(1) } returns user
        val invalid = WeeklyTimeConstraintDto(null, true, "invalid", TimeFrame(DayOfWeek.MONDAY, LocalTime.now(), LocalTime.now()))

        assertThrows<Exception> { service.save(invalid, userDto) }
    }

    @Test
    fun saveInvalidDailyConstraint(){
        every { us.getById(1) } returns user
        val invalid = DailyTimeConstraintDto(null, true, "invalid", LocalDateTime.now(), LocalDateTime.now())

        assertThrows<Exception> { service.save(invalid, userDto) }
    }

    @Test
    fun deleteNonExistentConstraint(){
        every { repo.findByIdOrNull(0) } returns null
        assertThrows<Exception> { service.delete(0, userDto) }
    }

    @Test
    fun deleteConstraintWrongUser(){
        every { repo.findByIdOrNull(1) } returns constraint
        assertThrows<Exception> { service.delete(1, UserDTO(2, "","", null, null, "")) }
    }

    @Test
    fun deleteConstraint() {
        every { repo.findByIdOrNull(1) } returns constraint
        every { repo.deleteById(1) } answers { }

        assertDoesNotThrow { service.delete(1, userDto) }
    }

    @Test
    fun getConstraints(){
        every { weeklyRepo.findByUser(user) } returns listOf(weeklyConstraint)
        every { us.getById(1) } returns user

        val list = service.getAll(userDto, "","","")
        val asDaily = service.getAll(userDto, "daily", "", "")

        assertAll(
            { assertEquals(list[0].id, weeklyConstraint.toDto().id) },
            { assertEquals(list[0].title, weeklyConstraint.toDto().title) },
            { assertTrue(list[0] is WeeklyTimeConstraintDto) },
            { assertTrue(asDaily[0] is DailyTimeConstraintDto) }
        )
    }

    @Test
    fun createDefaultTimeConstraints_shouldSaveSevenWeeklyConstraintsForUser(){

        every { weeklyRepo.save(any()) } returns weeklyConstraint

        service.createDefaultTimeConstraintsForUser(user)

        verify(exactly = 7) { weeklyRepo.save(any<WeeklyTimeConstraint>()) }
    }

}
