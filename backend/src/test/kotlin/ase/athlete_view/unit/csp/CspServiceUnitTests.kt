package ase.athlete_view.unit.csp

import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.common.exception.entity.ValidationException
import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.csp.pojo.dto.CspActivityDto
import ase.athlete_view.domain.csp.pojo.dto.CspDto
import ase.athlete_view.domain.csp.pojo.dto.CspMappingDto
import ase.athlete_view.domain.csp.service.CspService
import ase.athlete_view.domain.csp.service.impl.CspServiceImpl.Companion.SLOT_DURATION
import ase.athlete_view.domain.csp.service.impl.CspServiceImpl.Companion.TOTAL_SLOTS
import ase.athlete_view.domain.csp.util.QueueRequestSender
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.util.ActivityCreator.Companion.getDefaultActivity
import ase.athlete_view.util.ActivityCreator.Companion.getTemplatePlannedActivity
import ase.athlete_view.util.TestBase
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ninjasquad.springmockk.MockkBean
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import java.lang.Exception
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@ActiveProfiles("test")
class CspServiceUnitTests : TestBase() {

    @MockkBean
    lateinit var qrs: QueueRequestSender

    @Autowired
    private lateinit var cspService: CspService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun testAcceptWorksCorrectly(){
        val argumentSlot: CapturingSlot<String> = slot()
        every {qrs.sendMessage(capture(argumentSlot))} answers { checkJson(argumentSlot.captured) }

        val cspActivityDto1 = CspActivityDto(-8,true)
        val cspActivityDto2 = CspActivityDto(-9,false)
        val cspMappingDto = CspMappingDto(-2,listOf(cspActivityDto1,cspActivityDto2))
        val cspDto = CspDto(listOf(cspMappingDto))
        cspService.accept(cspDto,-3)

    }

    fun checkJson(msg: String){
        println(msg)
        val resultMap:Map<String, Any> = objectMapper.readValue(msg)
        val activities: List<Map<String, Any>> = resultMap["activities"] as List<Map<String, Any>>
        val countWrongId = activities.filter { it["id"] == "-8" || it["id"] == "-9"}
        val countWithTrainerFalse = activities.filter { it["withTrainer"] == false }.count()
        val schedule: Map<String, Any> = resultMap["schedule"] as Map<String, Any>
        val trainerTable: List<List<Boolean>> = schedule["trainerTable"] as List<List<Boolean>>
        val athleteTables: Map<String, List<List<Boolean>>> = schedule["athleteTables"] as Map<String, List<List<Boolean>>>
        val athleteTable: List<List<Boolean>>? = athleteTables["-2"]
        val countTrueValues: Int = athleteTable?.sumOf { it.count { value -> value } } ?: 0
        assertAll(
                { assert((resultMap["trainerId"] as Int) == -3) }, //trainerId correct
                { assert((resultMap["requestTimestamp"] as Long) > Instant.now().toEpochMilli() - 5*60*1000)}, //requestTimestamp correct
                { assert(countWrongId.isEmpty())}, // check activities were duplicated
                { assert(activities.size ==2 )}, // check exactly 2 activities
                { assert(countWithTrainerFalse==1)}, //check duplicate with other withTrainer bool value is correct
                { assert((schedule["trainerId"] as Int) == -3)}, //trainerId correct
                { assert(trainerTable.size == 7 && trainerTable[0].size == TOTAL_SLOTS)}, //check correct size
                { assert(athleteTables["-2"]!!.size == 7 && athleteTables["-2"]!![0].size == TOTAL_SLOTS)}, //check correct size
                { assert(listOf(0,1,2).contains(activities[0]["intensity"]))}, //check intensity correctly mapped
                { assert(activities[0]["duration"] == (60/SLOT_DURATION))}, //check duration correctly calculated
                { assert(countTrueValues == 44)}, // check constraints correctly substracted
        )
    }

    @Test
    fun testAcceptFailsTooManyActivities(){

        every {qrs.sendMessage(any())} returns Unit

        val cspActivityDto1 = CspActivityDto(-8,true)
        val cspActivityDto2 = CspActivityDto(-9,false)
        val cspActivityDto3 = CspActivityDto(-9,false)
        val cspActivityDto4 = CspActivityDto(-9,false)
        val cspActivityDto5 = CspActivityDto(-9,false)
        val cspActivityDto6 = CspActivityDto(-9,false)
        val cspActivityDto7 = CspActivityDto(-9,false)
        val cspActivityDto8 = CspActivityDto(-9,false)
        val cspMappingDto = CspMappingDto(-2,listOf(cspActivityDto1,cspActivityDto2,cspActivityDto3,cspActivityDto4,cspActivityDto5,cspActivityDto6,cspActivityDto7,cspActivityDto8))
        val cspDto = CspDto(listOf(cspMappingDto))
        assertThrows<ValidationException>{cspService.accept(cspDto,-3)}
    }

    @Test
    fun testAcceptFailsUserIsNotTrainer(){
        every {qrs.sendMessage(any())} returns Unit

        val cspActivityDto1 = CspActivityDto(-8,true)
        val cspActivityDto2 = CspActivityDto(-9,false)
        val cspMappingDto = CspMappingDto(-2,listOf(cspActivityDto1,cspActivityDto2))
        val cspDto = CspDto(listOf(cspMappingDto))
        assertThrows<ValidationException>{cspService.accept(cspDto,-2)}
    }

    @Test
    fun testAcceptFailsAthleteIsNotAssignedToTrainer(){
        every {qrs.sendMessage(any())} returns Unit

        val cspActivityDto1 = CspActivityDto(-8,true)
        val cspActivityDto2 = CspActivityDto(-9,false)
        val cspMappingDto = CspMappingDto(-1,listOf(cspActivityDto1,cspActivityDto2))
        val cspDto = CspDto(listOf(cspMappingDto))
        assertThrows<ValidationException>{cspService.accept(cspDto,-3)}
    }

    @Test
    fun testAcceptFailsTooMuchIntensity(){
        every {qrs.sendMessage(any())} returns Unit

        val cspActivityDto = CspActivityDto(-8,true)
        val cspMappingDto = CspMappingDto(-1,listOf(cspActivityDto,cspActivityDto,cspActivityDto,cspActivityDto,cspActivityDto))
        val cspDto = CspDto(listOf(cspMappingDto))
        assertThrows<ValidationException>{cspService.accept(cspDto,-3)}
    }

    @Test
    fun testGetJobExistsReturnsJob(){
        every {qrs.sendMessage(any())} returns Unit
        val cspActivityDto1 = CspActivityDto(-8,true)
        val cspActivityDto2 = CspActivityDto(-9,false)
        val cspMappingDto = CspMappingDto(-2,listOf(cspActivityDto1,cspActivityDto2))
        val cspDto = CspDto(listOf(cspMappingDto))
        cspService.accept(cspDto,-3)
        val job = cspService.getJob(-3)
        assert(job != null)
    }

    @Test
    fun testGetJobNotExistsReturnsNull(){
        val job = cspService.getJob(-3)
        assert(job == null)
    }

    @Test
    fun testRevertJobRevertsCorrectly(){
        every {qrs.sendMessage(any())} returns Unit
        val cspActivityDto1 = CspActivityDto(-8,true)
        val cspActivityDto2 = CspActivityDto(-9,false)
        val cspMappingDto = CspMappingDto(-2,listOf(cspActivityDto1,cspActivityDto2))
        val cspDto = CspDto(listOf(cspMappingDto))
        cspService.accept(cspDto,-3)
        val job = cspService.getJob(-3)
        assert(job != null)
        cspService.revertJob(-3)
        val job2 = cspService.getJob(-3)
        assert(job2 == null)
    }

}