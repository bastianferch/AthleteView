package ase.athlete_view.unit

import ase.athlete_view.common.exception.fitimport.InvalidFitFileException
import ase.athlete_view.domain.activity.service.ActivityService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import util.ContainerizedMongoTest
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.absolute

//@SpringBootTest
//@ActiveProfiles("test")
@ContainerizedMongoTest
class ActivityServiceMongoUnitTests {
    @Autowired
    private lateinit var activityService: ActivityService

    @Test
    fun uploadingValidIFitFile_shouldSucceed() {
        val filePath = Paths.get("src/test/resources/fit-files/valid_file.fit").absolute()
        val name = "test.fit"
        val content = "text/plain"
        val byteContent = Files.readAllBytes(filePath)
        val resultFile = MockMultipartFile(name, name, content, byteContent)

        val result = activityService.importActivity(arrayListOf(resultFile))
        Assertions.assertNotNull(result)
        Assertions.assertEquals(1, result.size)
        Assertions.assertNotEquals("", result[0])
    }

    @Test
    fun uploadingEmptyButValidIFitFile_shouldSucceed() {
        val filePath = Paths.get("src/test/resources/fit-files/valid_file_2.fit").absolute()
        val name = "test.fit"
        val content = "text/plain"
        val byteContent = Files.readAllBytes(filePath)
        val resultFile = MockMultipartFile(name, name, content, byteContent)

        val result = activityService.importActivity(arrayListOf(resultFile))
        Assertions.assertNotNull(result)
        Assertions.assertEquals(0, result.size)
    }

    @Test
    fun uploadingInvalidIFitFile_shouldThrowException() {
        val filePath = Paths.get("src/test/resources/fit-files/empty_file.fit").absolute()
        val name = "login"
        val content = "text/plain"
        val byteContent = Files.readAllBytes(filePath)
        val resultFile = MockMultipartFile(name, name, content, byteContent)

        assertThrows<InvalidFitFileException> { activityService.importActivity(arrayListOf(resultFile)) }
    }
}