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
import ase.athlete_view.domain.mail.service.MailService
import ase.athlete_view.domain.notification.persistence.EmitterRepository
import ase.athlete_view.domain.notification.persistence.NotificationRepository
import ase.athlete_view.domain.notification.pojo.entity.Notification
import ase.athlete_view.domain.notification.service.NotificationService
import ase.athlete_view.util.TestBase
import ase.athlete_view.util.WithCustomMockUser
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
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

const val TEST_USER_ID = -1L

@SpringBootTest(
    classes = [AthleteViewApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationControllerIntegrationTest : TestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var notificationService: NotificationService

    @Autowired
    private lateinit var notificationRepository: NotificationRepository

    @Autowired
    private lateinit var emitterRepository: EmitterRepository

    @MockkBean
    private lateinit var mailService: MailService

    @AfterEach
    fun wipeEmitterRepo() {
        emitterRepository.deleteAll()
    }

    @AfterEach
    fun wipeNotificationRepo() {
        notificationRepository.deleteAll()
    }

    fun sendUserNotification(userId: Long): Notification? {
        return notificationService.sendNotification(userId, "header", "body", "link")
    }

    @Test
    @WithCustomMockUser(id = TEST_USER_ID)
    fun subscribeToNotifications_shouldCreateEmitterAndReturnOk() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/notification/subscribe").with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk())

        // check if there is an emitter for this user now
        assertTrue(emitterRepository.existsById(TEST_USER_ID))
    }

    @Test
    @WithCustomMockUser(id = TEST_USER_ID)
    fun subscribeToNotificationsTwice_shouldCreateEmitterAndReturnOk() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/notification/subscribe").with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk())

        // doing this a second time should replace the emitter
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/notification/subscribe").with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk())

        // check if there is an emitter for this user now
        assertTrue(emitterRepository.existsById(TEST_USER_ID))
    }


    @Test
    @WithCustomMockUser(id = TEST_USER_ID)
    fun getAllNotifications_shouldReturnEmptyListAndOk() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/notification").with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(0))
            .andReturn().response.contentAsString
    }

    @Test
    @WithCustomMockUser(id = TEST_USER_ID)
    fun getAllNotificationsWithUserWithOneNotification_shouldReturnListAndOk() {
        // make sure mail service doesn't send anything
        every { mailService.sendSimpleMail(any()) } returns Unit

        // send user a notification
        notificationService.sendNotification(TEST_USER_ID, "header", "body", "link")

        val notifications = notificationService.getAllNotifications(TEST_USER_ID)

        assertEquals(notifications.size, 1)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/notification").with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray)
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1))
            .andReturn().response.contentAsString
    }


    @Test
    @WithCustomMockUser(TEST_USER_ID)
    fun deleteNonexistentNotification_shouldReturnNotFound() {

        //get a nonexistent id
        var nonexistentNotificationId = 9999L
        while (notificationRepository.existsById(nonexistentNotificationId)) {
            nonexistentNotificationId++
        }

        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/notification/$nonexistentNotificationId")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isNotFound())
    }

    @Test
    @WithCustomMockUser(TEST_USER_ID)
    fun deleteNotification_shouldDeleteNotificationAndReturnOk() {
        val notification = sendUserNotification(TEST_USER_ID)
        assertNotNull(notification)

        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/notification/${notification!!.id}")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk())

        // check if really deleted
        assertTrue(!notificationRepository.existsById(notification.id!!))
    }

    @Test
    @WithCustomMockUser(TEST_USER_ID)
    fun deleteNotification_shouldOnlyDeleteRightNotificationAndReturnOk() {
        val notification = sendUserNotification(TEST_USER_ID)
        val notification2 = sendUserNotification(TEST_USER_ID)

        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/notification/${notification!!.id}")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk())

        // assert that notification was deleted
        assertTrue(!notificationRepository.existsById(notification.id!!))

        // assert that notification2 was not deleted
        assertTrue(notificationRepository.existsById(notification2!!.id!!))
    }

    @Test
    @WithCustomMockUser(TEST_USER_ID)
    fun deleteAllNotifications_shouldDeleteNotificationsAndReturnOk() {
        sendUserNotification(TEST_USER_ID)
        sendUserNotification(TEST_USER_ID)
        sendUserNotification(TEST_USER_ID)

        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/notification").with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk())

        assertEquals(notificationRepository.count(), 0L)
    }

    @Test
    @WithCustomMockUser(TEST_USER_ID)
    fun markAllNotificationsAsRead_shouldMarkAllAsReadAndReturnOk() {

        // send two notifications
        sendUserNotification(TEST_USER_ID)
        sendUserNotification(TEST_USER_ID)

        // mark both as read
        mockMvc.perform(
            MockMvcRequestBuilders.patch("/api/notification").with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk())

        // check if notifications were marked as read
        val notifs = notificationService.getAllNotifications(TEST_USER_ID)
        assertEquals(notifs.size, 2)
        assertTrue(notifs[0].read)
        assertTrue(notifs[1].read)
    }

    @Test
    @WithCustomMockUser(TEST_USER_ID + 1) // wrong user
    fun markAllNotificationsAsReadAsWrongUser_shouldDoNothingAndReturnOk() {

        // send two notifications
        sendUserNotification(TEST_USER_ID)
        sendUserNotification(TEST_USER_ID)

        // mark both as read
        mockMvc.perform(
            MockMvcRequestBuilders.patch("/api/notification").with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk())

        // check if notifications were marked as read
        val notifs = notificationService.getAllNotifications(TEST_USER_ID)
        assertEquals(notifs.size, 2)
        assertTrue(!notifs[0].read)
        assertTrue(!notifs[1].read)
    }

    @Test
    @WithCustomMockUser(TEST_USER_ID)
    fun markAllNotificationsAsReadTwice_shouldMarkAllAsReadAndReturnOk() {

        // send two notifications
        sendUserNotification(TEST_USER_ID)
        sendUserNotification(TEST_USER_ID)

        // mark both as read
        mockMvc.perform(
            MockMvcRequestBuilders.patch("/api/notification").with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk())
        mockMvc.perform(
            MockMvcRequestBuilders.patch("/api/notification").with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk())

        // check if notifications were marked as read
        val notifications = notificationService.getAllNotifications(TEST_USER_ID)
        assertEquals(notifications.size, 2)
        assertTrue(notifications[0].read)
        assertTrue(notifications[1].read)
    }
}
