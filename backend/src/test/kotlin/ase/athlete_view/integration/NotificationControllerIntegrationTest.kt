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
import io.github.oshai.kotlinlogging.KotlinLogging
import io.mockk.every
import org.junit.jupiter.api.AfterEach
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

const val TEST_USER_ID = -1L;

@SpringBootTest(
    classes = [AthleteViewApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationControllerIntegrationTest: TestBase() {

    val logger = KotlinLogging.logger {}

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
        emitterRepository.deleteAll();
    }

    @AfterEach
    fun wipeNotificationRepo() {
        notificationRepository.deleteAll();
    }

    fun sendUserNotification(userId: Long): Notification? {
        return notificationService.sendNotification(userId, "header", "body", "link");
    }

    @Test
    @WithCustomMockUser(id = TEST_USER_ID)
    fun subscribeToNotifications_shouldCreateEmitterAndReturnOk() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/notification/subscribe").with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk())

        // check if there is an emitter for this user now
        assert(emitterRepository.existsById(TEST_USER_ID))
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
        assert(emitterRepository.existsById(TEST_USER_ID))
    }


    @Test
    @WithCustomMockUser(id = TEST_USER_ID)
    fun getAllNotifications_shouldReturnEmptyListAndOk() {
        val result = mockMvc.perform(
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
        notificationService.sendNotification(TEST_USER_ID, "header", "body", "link");

        val notifications = notificationService.getAllNotifications(TEST_USER_ID)

        logger.info { notifications }

        assert(notifications.size == 1)

        val result= mockMvc.perform(
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
            MockMvcRequestBuilders.delete("/api/notification/$nonexistentNotificationId").with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isNotFound())
    }

    @Test
    @WithCustomMockUser(TEST_USER_ID)
    fun deleteNotification_shouldDeleteNotificationAndReturnOk() {
        val notification = sendUserNotification(TEST_USER_ID)
        assert(notification != null)

        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/notification/${notification!!.id}").with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk())

        // check if really deleted
        assert(!notificationRepository.existsById(notification.id!!))
    }

    @Test
    @WithCustomMockUser(TEST_USER_ID)
    fun deleteNotification_shouldOnlyDeleteRightNotificationAndReturnOk() {
        val notification = sendUserNotification(TEST_USER_ID)
        val notification2 = sendUserNotification(TEST_USER_ID)

        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/notification/${notification!!.id}").with(SecurityMockMvcRequestPostProcessors.csrf())
        ).andExpect(MockMvcResultMatchers.status().isOk())

        // assert that notification was deleted
        assert(!notificationRepository.existsById(notification.id!!))

        // assert that notiication2 was not deleted
        assert(notificationRepository.existsById(notification2!!.id!!))
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

        assert(notificationRepository.count() == 0L)
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
        assert(notifs.size == 2)
        assert(notifs[0].read)
        assert(notifs[1].read)
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
        assert(notifs.size == 2)
        assert(!notifs[0].read)
        assert(!notifs[1].read)
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
        assert(notifications.size == 2)
        assert(notifications[0].read)
        assert(notifications[1].read)
    }
}