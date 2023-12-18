package ase.athlete_view.config.profile.datagen

import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.activity.util.FitParser
import ase.athlete_view.domain.user.pojo.entity.User
import com.garmin.fit.DateTime
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.serpro69.kfaker.Faker
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import org.springframework.mock.web.MockMultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.TemporalAdjusters
import java.util.stream.Collectors


@Component
@Profile("datagen")
class ActivityDatagen(private val fitParser: FitParser, private val activityService: ActivityService) {
    var files = mutableListOf<MultipartFile>()

    var log = KotlinLogging.logger {}
    var faker = Faker()

    @PostConstruct
    fun init() {
        val currentDirectory = System.getProperty("user.dir")
        val dir: Path = if (currentDirectory.contains("backend")) {
            Paths.get("/src/main/resources/fit-files/")
        } else {
            Paths.get("backend/src/main/resources/fit-files/")
        }

        files = Files.list(dir)
            .filter {
                Files.isRegularFile(it)
            }
            .map { pathToFile ->
                val byteContent = Files.readAllBytes(pathToFile)

                MockMultipartFile(pathToFile.fileName.toString(), byteContent)

            }
            .collect(Collectors.toList())
    }

    fun changeFiles(addSpeed: Float, addHeartRate: Int, user: User) {
        files.forEach {
            val data = fitParser.decode(it.inputStream)
            val hour = faker.random.nextInt(6, 18)
            val minute = faker.random.randomValue(listOf(0, 15, 30, 45))
            for (d in data.recordMesgs) {
                if (d == data.recordMesgs[0] || d == data.recordMesgs[data.recordMesgs.size - 1]) {
                    val date = LocalDateTime.ofEpochSecond(d.timestamp.timestamp + 631065600, 0, ZoneOffset.UTC)
                    val lastWeek = LocalDateTime.now().minusWeeks(1).with(TemporalAdjusters.previousOrSame(date.dayOfWeek))
                    val newDateTime = DateTime(
                        lastWeek.withHour(hour).withMinute(minute).withSecond(date.second)
                            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1000 - 631065600
                    )
                    if (d == data.recordMesgs[data.recordMesgs.size - 1]) {
                        newDateTime.add(data.sessionMesgs[0].totalElapsedTime.toLong())
                    }
                    d.timestamp = newDateTime
                }
                if (d.enhancedSpeed != null) {
                    d.enhancedSpeed += addSpeed
                }
                if (d.heartRate != null) {
                    d.heartRate = (d.heartRate + addHeartRate).toShort()
                }
            }

            activityService.calculateStats(data, user, modifyMultipartFile(it))
        }
    }

    fun modifyMultipartFile(originalFile: MultipartFile): MultipartFile {
        val byteArray = (faker.pokemon.names() + faker.harryPotter.spells() + faker.bojackHorseman.tongueTwisters()).toByteArray()


        val originalBytes = originalFile.bytes
        val combinedBytes = originalBytes + byteArray

        return MockMultipartFile(
            originalFile.name,
            originalFile.originalFilename,
            originalFile.contentType,
            combinedBytes
        )
    }
}