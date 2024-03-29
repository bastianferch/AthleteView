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

import ase.athlete_view.common.exception.entity.ValidationException
import ase.athlete_view.domain.user.service.AthleteService
import ase.athlete_view.domain.zone.persistence.ZoneRepository
import ase.athlete_view.domain.zone.pojo.entity.Zone
import ase.athlete_view.domain.zone.pojo.entity.toDTO
import ase.athlete_view.domain.zone.service.ZoneService
import ase.athlete_view.util.UserCreator
import com.ninjasquad.springmockk.MockkBean
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class ZoneServiceUnitTests {

    @MockkBean
    private lateinit var zoneRepo: ZoneRepository

    @MockkBean
    private lateinit var athleteService: AthleteService

    @Autowired
    private lateinit var zoneService: ZoneService

    @Test
    fun getZones_shouldReturnZonesInCorrectOrder() {
        every { athleteService.getById(any()) } returns UserCreator.getAthlete(null)
        every { zoneRepo.findAllByUser(any()) } returns zoneService.calcZones(195).shuffled()

        val list = zoneService.getAll(1)

        assertAll(
            { assertEquals(list.size, 5) },
            { assertEquals(list[0].fromBPM, 0) },
            { assertTrue(list[4].fromBPM > list[3].fromBPM) },
        )
    }

    @Test
    fun editZonesWithInvalidZones_shouldThrowValidationException() {
        val validList = zoneService.calcZones(195)
        every { zoneRepo.saveAll(any<List<Zone>>()) } returns validList
        every { athleteService.getById(any()) } returns UserCreator.getAthlete(null)
        every { zoneRepo.deleteByUser(any()) } just Runs

        val invalidList = validList.map { z: Zone -> z.toDTO()}
        invalidList.forEach { it.fromBPM = -1 }
        val invalidSecondList = validList.filter { it.fromBPM !in 121..149 }.map { z: Zone -> z.toDTO()}

        assertAll(
            { assertThrows<ValidationException> { zoneService.edit(1, invalidList) } },
            { assertThrows<ValidationException> { zoneService.edit(1, invalidSecondList) }}
        )
    }

    @Test
    fun saveZoneWithValidZones_shouldSaveZonesCorrectly() {
        val list = zoneService.calcZones(20)
        val user = UserCreator.getAthlete(null)
        list.forEach { it.user = user }
        every { zoneRepo.saveAll(any<List<Zone>>()) } returns list
        every { athleteService.getById(any()) } returns user
        every { zoneRepo.deleteByUser(any()) } just Runs

        val saved = zoneService.edit(1, zoneService.calcZones(195).map { z:Zone -> z.toDTO() })

        verify(exactly = 1) { zoneRepo.deleteByUser(user) }

        assertAll(
            { assertEquals(saved.size, 5) },
            { assertEquals(saved[0].fromBPM, 0) }
        )
    }

    @Test
    fun resetZonesWithoutCustomMHR_shouldCreateNewZonesForAthlete() {
        val user = UserCreator.getAthlete(null)
        every { zoneRepo.saveAll(any<List<Zone>>()) } returnsArgument 0
        every { athleteService.getById(any()) } returns user
        every { zoneRepo.deleteByUser(any()) } just Runs


        val testList = zoneService.calcZones(195)
        val saved = zoneService.resetZones(1)

        verify(exactly = 1) { zoneRepo.deleteByUser(user) }

        assertAll(
            { assertEquals(saved.size, 5) },
            { assertEquals(saved[0].fromBPM, 0 ) },
            { assertNotSame(testList[0], saved[0]) },
            { assertNotSame(testList[4], saved[4]) },
        )
    }
}
