package ase.athlete_view.unit

import ase.athlete_view.common.exception.entity.ValidationException
import ase.athlete_view.domain.user.service.AthleteService
import ase.athlete_view.domain.zone.persistence.ZoneRepository
import ase.athlete_view.domain.zone.pojo.entity.Zone
import ase.athlete_view.domain.zone.pojo.entity.toDTO
import ase.athlete_view.domain.zone.service.ZoneService
import ase.athlete_view.util.UserCreator
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import junit.framework.TestCase.*
import org.junit.Test
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
    fun getZones() {
        every { athleteService.getById(any()) } returns UserCreator.getAthlete()
        every { zoneRepo.findAllByUser(any()) } returns zoneService.calcZones(20).shuffled()

        val list = zoneService.getAll(1)

        assertAll(
            { assertEquals(list.size, 5) },
            { assertTrue(list[0].fromBPM == 0) },
            { assertTrue(list[4].fromBPM > list[3].fromBPM) },
        )
    }

    @Test
    fun verifyZoneValidation() {
        val validList = zoneService.calcZones(20)
        every { zoneRepo.saveAll(any<List<Zone>>()) } returns validList
        every { athleteService.getById(any()) } returns UserCreator.getAthlete()
        every { zoneRepo.deleteByUser(any()) }

        val invalidList = validList.map { z: Zone -> z.toDTO()}
        invalidList.forEach { it.fromBPM = -1 }
        val invalidSecondList = validList.filter { it.fromBPM !in 121..149 }.map { z: Zone -> z.toDTO()}

        assertAll(
            { assertThrows<ValidationException> { zoneService.edit(1, invalidList) } },
            { assertThrows<ValidationException> { zoneService.edit(1, invalidSecondList) }}
        )
    }

    @Test
    fun saveZones() {
        val list = zoneService.calcZones(20)
        list.forEach { it.user = UserCreator.getAthlete() }
        every { zoneRepo.saveAll(any<List<Zone>>()) } returns list
        every { athleteService.getById(any()) } returns UserCreator.getAthlete()
        every { zoneRepo.deleteByUser(any()) }

        val saved = zoneService.edit(1, zoneService.calcZones(20).map { z:Zone -> z.toDTO() })

        verify(exactly = 1) { zoneRepo.deleteByUser(UserCreator.getAthlete()) }

        assertAll(
            { assertEquals(saved.size, 5) },
            { assertEquals(saved[0].fromBPM, 0 ) }
        )
    }

    @Test
    fun resetZones() {
        every { zoneRepo.saveAll(any<List<Zone>>()) } returnsArgument 0
        every { athleteService.getById(any()) } returns UserCreator.getAthlete()
        every { zoneRepo.deleteByUser(any()) }


        val testList = zoneService.calcZones(20)
        val saved = zoneService.resetZones(1)

        verify(exactly = 1) { zoneRepo.deleteByUser(UserCreator.getAthlete()) }

        assertAll(
            { assertEquals(saved.size, 5) },
            { assertEquals(saved[0].fromBPM, 0 ) },
            { assertNotSame(testList[0], saved[0]) },
            { assertNotSame(testList[4], saved[4]) },
        )
    }
}