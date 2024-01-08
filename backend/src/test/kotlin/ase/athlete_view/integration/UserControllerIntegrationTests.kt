package ase.athlete_view.integration

import ase.athlete_view.domain.mail.service.MailService
import ase.athlete_view.domain.user.controller.UserController
import ase.athlete_view.domain.user.persistence.PreferencesRepository
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.NotificationPreferenceType
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.domain.user.service.UserService
import ase.athlete_view.util.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.github.oshai.kotlinlogging.KotlinLogging
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*


@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTests: TestBase(){
    val log = KotlinLogging.logger {}

    @Autowired
    private lateinit var userController: UserController

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var athleteService: UserService

    @Autowired
    private lateinit var preferencesRepository: PreferencesRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockK
    private lateinit var mailService: MailService

    val objectMapper = ObjectMapper().registerModules(JavaTimeModule())

    @Test
    @WithCustomMockUser(ATHLETE_ID)
    fun getAthlete_shouldReturnOk() {
        mockMvc.perform(
            get("/api/user").with(csrf())
                .contentType("application/json")
                .characterEncoding("utf-8")
        ).andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("id").value(-2))
            .andExpect(jsonPath("email").value("b@a.com"))
    }

    @Test
    @WithCustomMockUser(TRAINER_ID)
    fun getAllAthletesByTrainerId_shouldReturnListOfAthletes() {
        mockMvc.perform(
            get("/api/user/athlete").with(csrf())
                .contentType("application/json")
                .characterEncoding("utf-8")
        ).andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize<Any>(1)))
            .andExpect(jsonPath("$[0].id").value(-2))
    }

    @Test
    @WithCustomMockUser(ATHLETE_ID)
    fun getAllAthletesByTrainerIdWithAthleteId_shouldReturnForbidden() {
        mockMvc.perform(
            get("/api/user/athlete").with(csrf())
                .contentType("application/json")
                .characterEncoding("utf-8")
        ).andExpect(status().isForbidden())
    }

    @Test
    @WithCustomMockUser(TRAINER_ID)
    fun resetCodeWithTrainer_shouldReturnNoContent() {
        mockMvc.perform(
            post("/api/user/trainer/code").with(csrf())
                .contentType("application/json")
                .characterEncoding("utf-8")
        ).andExpect(status().isNoContent())
    }

    @Test
    @WithCustomMockUser(ATHLETE_ID)
    fun resetCodeWithAthlete_shouldReturnForbidden() {
        mockMvc.perform(
            post("/api/user/trainer/code").with(csrf())
                .contentType("application/json")
                .characterEncoding("utf-8")
        ).andExpect(status().isForbidden())
    }

    @Test
    @WithCustomMockUser(TRAINER_ID)
    fun sendInvitationWithTrainer_shouldReturnOk() {
        every { mailService.sendSimpleMail(any()) } returns Unit
        val mailList = listOf("test@gmail.com", "test1@gmail.com")
        mockMvc.perform(
            post("/api/user/trainer/invitation").with(csrf())
                .contentType("application/json")
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(mailList))
        ).andExpect(status().isOk())
    }

    @Test
    @WithCustomMockUser(ATHLETE_ID)
    fun sendInvitationWithAthlete_shouldReturnForbidden() {
        val mailList = listOf("test@gmail.com", "test1@gmail.com")
        mockMvc.perform(
            post("/api/user/trainer/invitation").with(csrf())
                .contentType("application/json")
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(mailList))
        ).andExpect(status().isForbidden())
    }

    @Test
    @WithCustomMockUser(TRAINER_ID)
    fun acceptAthleteWithoutInvitation_shouldReturnNotFound(){
        mockMvc.perform(
            post("/api/user/trainer/athlete").with(csrf())
                .contentType("application/json")
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(1000L))
        ).andExpect(status().isNotFound)
    }

    @Test
    @WithCustomMockUser(TRAINER_ID)
    fun acceptAthleteAccepted_shouldReturnForbidden(){
        every { mailService.sendSimpleMail(any()) } returns Unit
        mockMvc.perform(
            post("/api/user/trainer/athlete").with(csrf())
                .contentType("application/json")
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(ATHLETE_ID))
        ).andExpect(status().isForbidden)
    }

    @Test
    @WithCustomMockUser(TRAINER_ID)
    fun acceptAthlete_shouldReturnNoContent(){
        val trainer = userService.getById(TRAINER_ID) as Trainer
        trainer.unacceptedAthletes.add(userService.getById(-4) as Athlete)
        userService.save(trainer)
        mockMvc.perform(
            post("/api/user/trainer/athlete").with(csrf())
                .contentType("application/json")
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(-4))
        ).andExpect(status().isNoContent)
    }

    @Test
    @WithCustomMockUser(TRAINER_ID)
    fun getPreferences_returnsDefaultPreferences(){
        val user = userService.getById(TRAINER_ID)
        mockMvc.perform(
            get("/api/user/preferences").with(csrf())
        ).andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.emailNotifications").value(false))
            .andExpect(jsonPath("$.commentNotifications").value("PUSH"))
            .andExpect(jsonPath("$.ratingNotifications").value("PUSH"))
            .andExpect(jsonPath("$.otherNotifications").value("PUSH"))

        //assert that it is really stored in the repository
        val prefsId = user.preferences?.id
        assert(prefsId != null)
        val newPrefs = preferencesRepository.getReferenceById(prefsId!!)
        assertAll(
            { assert(!newPrefs.emailNotifications) },
            { assert(newPrefs.commentNotifications == NotificationPreferenceType.PUSH) },
            { assert(newPrefs.ratingNotifications == NotificationPreferenceType.PUSH) },
            { assert(newPrefs.otherNotifications == NotificationPreferenceType.PUSH) },
        )
    }

    @Test
    @WithCustomMockUser(TRAINER_ID)
    fun patchPreferences_returnsNewPreferences(){
        val user = userService.getById(TRAINER_ID)
        // new preferences (different from default prefs)
        val prefsDto = UserCreator.getPreferencesDto()
        mockMvc.perform(
            patch("/api/user/preferences").with(csrf())
                .contentType("application/json")
                .characterEncoding("utf-8")
                .content(objectMapper.writeValueAsString(prefsDto))
        ).andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.emailNotifications").value(prefsDto.emailNotifications))
            .andExpect(jsonPath("$.commentNotifications").value("NONE"))
            .andExpect(jsonPath("$.ratingNotifications").value("NONE"))
            .andExpect(jsonPath("$.otherNotifications").value("NONE"))

        //assert that it is really stored in the repository
        val prefsId = user.preferences?.id
        assert(prefsId != null)
        val newPrefs = preferencesRepository.getReferenceById(prefsId!!)

        assertAll(
            { assert(newPrefs.emailNotifications) },
            { assert(newPrefs.commentNotifications == NotificationPreferenceType.NONE) },
            { assert(newPrefs.ratingNotifications == NotificationPreferenceType.NONE) },
            { assert(newPrefs.otherNotifications == NotificationPreferenceType.NONE) },
        )
    }
}
