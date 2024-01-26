package ase.athlete_view.unit

import ase.athlete_view.common.exception.fitimport.InvalidFitFileException
import ase.athlete_view.domain.activity.controller.ActivityController
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.util.ActivityCreator
import ase.athlete_view.util.WithCustomMockUser
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.absolute


@WebMvcTest(controllers = [ActivityController::class])
@ActiveProfiles("test")
class ActivityServiceControllerUnitTests {
    @MockkBean
    private lateinit var activityService: ActivityService

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @WithCustomMockUser
    fun verifyUploadingValidFitFile_Returns201Created() {
        // syncWithMockServer service
        every { activityService.importActivity(any<List<MultipartFile>>(), any<Long>()) } returns ActivityCreator.getDefaultActivity()

        val filePath = Paths.get("src/test/resources/fit-files/valid_file.fit").absolute()
        val name = "test.fit"
        val byteContent = Files.readAllBytes(filePath)
        val resultFile = MockMultipartFile("files", name, MediaType.MULTIPART_FORM_DATA_VALUE, byteContent)

        mockMvc.perform(
            multipart(HttpMethod.POST, "/api/activity/import")
                .file(resultFile)
                .with(csrf())
        )
            .andExpect(status().isCreated)

        verify(exactly = 1) { activityService.importActivity(any<List<MultipartFile>>(), any<Long>()) }
    }

    @Test
    @WithCustomMockUser
    fun mockActivity_Returns201Created() {
        every { activityService.syncWithMockServer(any()) } just runs

        mockMvc.perform(
            multipart(HttpMethod.POST, "/api/activity/sync")
                .with(csrf())
        )
            .andExpect(status().isCreated)

        verify(exactly = 1) { activityService.syncWithMockServer(any()) }
    }

    @Test
    @WithCustomMockUser
    fun verifyUploadingInvalidFitFile_Returns400BadRequest() {
        // syncWithMockServer service
        every { activityService.importActivity(any<List<MultipartFile>>(), any<Long>()) } throws InvalidFitFileException("invalid")

        val filePath = Paths.get("src/test/resources/fit-files/empty_file.fit").absolute()
        val name = "test.fit"
        val byteContent = Files.readAllBytes(filePath)
        val resultFile = MockMultipartFile("files", name, MediaType.MULTIPART_FORM_DATA_VALUE, byteContent)

        mockMvc.perform(
            multipart(HttpMethod.POST, "/api/activity/import")
                .file(resultFile)
                .with(csrf())
        )
            .andExpect(status().isBadRequest)

        verify(exactly = 1) { activityService.importActivity(any<List<MultipartFile>>(), any<Long>()) }
    }
}