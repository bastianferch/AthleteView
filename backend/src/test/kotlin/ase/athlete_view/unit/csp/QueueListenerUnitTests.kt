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

import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.pojo.util.ActivityType
import ase.athlete_view.domain.activity.pojo.util.Load
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.csp.pojo.dto.CspActivityDto
import ase.athlete_view.domain.csp.pojo.dto.CspDto
import ase.athlete_view.domain.csp.pojo.dto.CspMappingDto
import ase.athlete_view.domain.csp.pojo.entity.CspJob
import ase.athlete_view.domain.csp.service.CspService
import ase.athlete_view.domain.csp.service.impl.CspServiceImpl
import ase.athlete_view.domain.csp.util.QueueResponseListener
import ase.athlete_view.domain.notification.pojo.entity.Notification
import ase.athlete_view.domain.notification.service.NotificationService
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.domain.user.service.UserService
import ase.athlete_view.util.ActivityCreator
import ase.athlete_view.util.TestBase
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import java.sql.Timestamp
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@ActiveProfiles("test")
class QueueListenerUnitTests : TestBase() {

    @MockkBean
    lateinit var ns: NotificationService

    @MockkBean
    lateinit var activityService: ActivityService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var queueResponseListener: QueueResponseListener

    @Autowired
    private lateinit var userService: UserService

    @MockkBean
    private lateinit var cspService: CspService

    @Test
    fun testError() {
        every { ns.sendNotification(any(), any(), any(), any(), any()) } returns null
        every { cspService.accept(any(), any()) } returns Unit
        every { cspService.getJob(any())} returns CspJob(0, mutableListOf<PlannedActivity>(), Trainer(null,"","","","","","",mutableSetOf<Athlete>(), mutableSetOf<Athlete>()),"")
        every { cspService.revertJob(any()) } returns Unit
        val json = "{\"success\":false,\"activities\":[],\"error\":\"test error\",\"requestTimestamp\":${Instant.now().toEpochMilli()},\"trainerId\":-3}".toByteArray()

        queueResponseListener.receiveMessage(json)
        verify { ns.sendNotification(-3, "Scheduling Job failed", "test error", "/trainingsplan") }
        verify { cspService.revertJob(-3) }
    }

    @Test
    fun testSuccess() {
        every { ns.sendNotification(match { it == -3L }, any(), any(), any()) } returns Notification(0, userService.getById(-3L), false, Timestamp(Instant.now().toEpochMilli()), "test", null, null)
        every { ns.sendNotification(match { it == -2L }, any(), any(), any()) } returns Notification(0, userService.getById(-2L), false, Timestamp(Instant.now().toEpochMilli()), "test", null, null)
        every { activityService.getPlannedActivity(match { it == -10L }, any()) } returns PlannedActivity(-10, "test", ActivityType.BIKE, ActivityCreator.getDefaultInterval(), true, false, "test", null, 60, Load.LOW, userService.getById(-3), userService.getById(-2) as Athlete, null)
        every { activityService.getPlannedActivity(match { it == -11L }, any()) } returns PlannedActivity(-11, "test", ActivityType.BIKE, ActivityCreator.getDefaultInterval(), true, false, "test", null, 60, Load.LOW, userService.getById(-3), userService.getById(-2) as Athlete, null)
        every { activityService.updatePlannedActivity(match { it == -10L }, any(), any()) } returns PlannedActivity(-10, "test", ActivityType.BIKE, ActivityCreator.getDefaultInterval(), true, false, "test", null, 60, Load.LOW, userService.getById(-3), userService.getById(-2) as Athlete, null)
        every { activityService.updatePlannedActivity(match { it == -11L }, any(), any()) } returns PlannedActivity(-11, "test", ActivityType.BIKE, ActivityCreator.getDefaultInterval(), true, false, "test", null, 60, Load.LOW, userService.getById(-3), userService.getById(-2) as Athlete, null)

        val activities = listOf("{\"id\":-10,\"scheduledDay\":3,\"scheduledStartTime\":32}",
                "{\"id\":-11,\"scheduledDay\":4,\"scheduledStartTime\":32}")
        val json = ("{\"success\":true,\"requestTimestamp\":${Instant.now().toEpochMilli()},\"trainerId\":-3," +
                "\"activities\":" + activities + ", \"threshold\": 0}")


        val today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0)
        val nextMonday = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).withHour(0).withMinute(0).withSecond(0).withNano(0)

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedLinkDate = nextMonday.format(formatter)

        val timestamp1: LocalDateTime = nextMonday.plusDays(3).withHour((32 * CspServiceImpl.SLOT_DURATION) / 60).withMinute((32 * CspServiceImpl.SLOT_DURATION) % 60)
        val timestamp2: LocalDateTime = nextMonday.plusDays(4).withHour((32 * CspServiceImpl.SLOT_DURATION) / 60).withMinute((32 * CspServiceImpl.SLOT_DURATION) % 60)

        queueResponseListener.receiveMessage(json.toByteArray())

        verify(exactly = 2) {
            activityService.updatePlannedActivity(match { it == -10L || it == -11L }, match { it.date == timestamp1 || it.date == timestamp2 }, any())
        }
        verify(exactly = 2) {
            ns.sendNotification(match { it == -3L || it == -2L }, any(), any(), "?date=${formattedLinkDate}")
        }
    }
}
