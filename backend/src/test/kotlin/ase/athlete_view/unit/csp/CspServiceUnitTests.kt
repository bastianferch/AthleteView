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
package ase.athlete_view.unit.csp

import ase.athlete_view.common.exception.entity.ValidationException
import ase.athlete_view.domain.csp.pojo.dto.CspActivityDto
import ase.athlete_view.domain.csp.pojo.dto.CspDto
import ase.athlete_view.domain.csp.pojo.dto.CspMappingDto
import ase.athlete_view.domain.csp.service.CspService
import ase.athlete_view.domain.csp.service.impl.CspServiceImpl.Companion.SLOT_DURATION
import ase.athlete_view.domain.csp.service.impl.CspServiceImpl.Companion.TOTAL_SLOTS
import ase.athlete_view.domain.csp.util.QueueRequestSender
import ase.athlete_view.domain.notification.service.NotificationService
import ase.athlete_view.util.TestBase
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ninjasquad.springmockk.MockkBean
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import java.time.Instant

@ActiveProfiles("test")
class CspServiceUnitTests : TestBase() {

    @MockkBean
    lateinit var qrs: QueueRequestSender

    @Autowired
    private lateinit var cspService: CspService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    lateinit var ns: NotificationService

    @Test
    fun testAcceptWorksCorrectly() {
        val argumentSlot: CapturingSlot<String> = slot()
        every { qrs.sendMessage(capture(argumentSlot)) } answers { checkJson(argumentSlot.captured) }

        val cspActivityDto1 = CspActivityDto(-8, true)
        val cspActivityDto2 = CspActivityDto(-9, false)
        val cspMappingDto = CspMappingDto(-2, listOf(cspActivityDto1, cspActivityDto2))
        val cspDto = CspDto(listOf(cspMappingDto))
        cspService.accept(cspDto, -3)

    }

    fun checkJson(msg: String) {
        val resultMap: Map<String, Any> = objectMapper.readValue(msg)
        val activities: List<Map<String, Any>> = resultMap["activities"] as List<Map<String, Any>>
        val countWrongId = activities.filter { it["id"] == "-8" || it["id"] == "-9" }
        val countWithTrainerFalse = activities.filter { it["withTrainer"] == false }.count()
        val schedule: Map<String, Any> = resultMap["schedule"] as Map<String, Any>
        val trainerTable: List<List<Boolean>> = schedule["trainerTable"] as List<List<Boolean>>
        val athleteTables: Map<String, List<List<Boolean>>> =
                schedule["athleteTables"] as Map<String, List<List<Boolean>>>
        val athleteTable: List<List<Boolean>>? = athleteTables["-2"]
        val countTrueValues: Int = athleteTable?.sumOf { it.count { value -> value } } ?: 0
        assertAll(
                { assertEquals((resultMap["trainerId"] as Int), -3) }, //trainerId correct
                {
                    assertTrue(
                            (resultMap["requestTimestamp"] as Long) > Instant.now().toEpochMilli() - 5 * 60 * 1000
                    )
                }, //requestTimestamp correct
                { assertTrue(countWrongId.isEmpty()) }, // check activities were duplicated
                { assertEquals(activities.size, 2) }, // check exactly 2 activities
                { assertEquals(countWithTrainerFalse, 1) }, //check duplicate with other withTrainer bool value is correct
                { assertEquals((schedule["trainerId"] as Int), -3) }, //trainerId correct
                { assertEquals(trainerTable.size, 7) }, //check correct size
                { assertEquals(trainerTable[0].size, TOTAL_SLOTS) }, //check correct size
                { assertEquals(athleteTables["-2"]!!.size, 7) }, //check correct size
                { assertEquals(athleteTables["-2"]!![0].size, TOTAL_SLOTS) }, //check correct size
                { assertTrue(listOf(0, 1, 2).contains(activities[0]["intensity"])) }, //check intensity correctly mapped
                { assertEquals(activities[0]["duration"], (60 / SLOT_DURATION)) }, //check duration correctly calculated
                { assertEquals(countTrueValues, 44) }, // check constraints correctly substracted
        )
    }

    @Test
    fun testAcceptFailsTooManyActivities() {

        every { qrs.sendMessage(any()) } returns Unit

        val cspActivityDto1 = CspActivityDto(-8, true)
        val cspActivityDto2 = CspActivityDto(-9, false)
        val cspActivityDto3 = CspActivityDto(-9, false)
        val cspActivityDto4 = CspActivityDto(-9, false)
        val cspActivityDto5 = CspActivityDto(-9, false)
        val cspActivityDto6 = CspActivityDto(-9, false)
        val cspActivityDto7 = CspActivityDto(-9, false)
        val cspActivityDto8 = CspActivityDto(-9, false)
        val cspMappingDto = CspMappingDto(
                -2,
                listOf(
                        cspActivityDto1,
                        cspActivityDto2,
                        cspActivityDto3,
                        cspActivityDto4,
                        cspActivityDto5,
                        cspActivityDto6,
                        cspActivityDto7,
                        cspActivityDto8
                )
        )
        val cspDto = CspDto(listOf(cspMappingDto))
        assertThrows<ValidationException> { cspService.accept(cspDto, -3) }
    }

    @Test
    fun testAcceptFailsUserIsNotTrainer() {
        every { qrs.sendMessage(any()) } returns Unit

        val cspActivityDto1 = CspActivityDto(-8, true)
        val cspActivityDto2 = CspActivityDto(-9, false)
        val cspMappingDto = CspMappingDto(-2, listOf(cspActivityDto1, cspActivityDto2))
        val cspDto = CspDto(listOf(cspMappingDto))
        assertThrows<ValidationException> { cspService.accept(cspDto, -2) }
    }

    @Test
    fun testAcceptFailsAthleteIsNotAssignedToTrainer() {
        every { qrs.sendMessage(any()) } returns Unit

        val cspActivityDto1 = CspActivityDto(-8, true)
        val cspActivityDto2 = CspActivityDto(-9, false)
        val cspMappingDto = CspMappingDto(-1, listOf(cspActivityDto1, cspActivityDto2))
        val cspDto = CspDto(listOf(cspMappingDto))
        assertThrows<ValidationException> { cspService.accept(cspDto, -3) }
    }

    @Test
    fun testAcceptFailsTooMuchIntensity() {
        every { qrs.sendMessage(any()) } returns Unit

        val cspActivityDto = CspActivityDto(-8, true)
        val cspMappingDto =
                CspMappingDto(-1, listOf(cspActivityDto, cspActivityDto, cspActivityDto, cspActivityDto, cspActivityDto))
        val cspDto = CspDto(listOf(cspMappingDto))
        assertThrows<ValidationException> { cspService.accept(cspDto, -3) }
    }

    @Test
    fun testGetJobExistsReturnsJob() {
        every { qrs.sendMessage(any()) } returns Unit
        val cspActivityDto1 = CspActivityDto(-8, true)
        val cspActivityDto2 = CspActivityDto(-9, false)
        val cspMappingDto = CspMappingDto(-2, listOf(cspActivityDto1, cspActivityDto2))
        val cspDto = CspDto(listOf(cspMappingDto))
        cspService.accept(cspDto, -3)
        val job = cspService.getJob(-3)
        assertNotNull(job)
    }

    @Test
    fun testGetJobNotExistsReturnsNull() {
        val job = cspService.getJob(-3)
        assertNull(job)
    }

    @Test
    fun
            testRevertJobRevertsCorrectly() {
        every { qrs.sendMessage(any()) } returns Unit
        every { ns.sendNotification(any(), any(), any(), any(), any()) } returns null

        val cspActivityDto1 = CspActivityDto(-8, true)
        val cspActivityDto2 = CspActivityDto(-9, false)
        val cspMappingDto = CspMappingDto(-2, listOf(cspActivityDto1, cspActivityDto2))
        val cspDto = CspDto(listOf(cspMappingDto))
        cspService.accept(cspDto, -3)
        val job = cspService.getJob(-3)
        assertNotNull(job)
        cspService.revertJob(-3)
        val job2 = cspService.getJob(-3)
        assertNull(job2)

        verify(exactly = 1) {
            ns.sendNotification(match { it == -2L }, "Current Trainingsplan reverted", any(), any())
        }
    }

}
