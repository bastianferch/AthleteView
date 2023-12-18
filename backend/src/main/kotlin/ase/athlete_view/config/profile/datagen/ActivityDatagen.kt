package ase.athlete_view.config.profile.datagen

import ase.athlete_view.domain.activity.util.FitParser
import com.garmin.fit.DateTime
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.serpro69.kfaker.Faker
import jakarta.annotation.PostConstruct
import org.springframework.core.io.FileSystemResource
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.TemporalAdjusters
import java.util.stream.Collectors

@Component
class ActivityDatagen(private val fitParser: FitParser) {
    var files = mutableListOf<FileSystemResource>()

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
                FileSystemResource(dir.toString() + "/" + pathToFile.fileName.toString())
            }
            .collect(Collectors.toList())
    }

    fun changeFiles(addSpeed: Float, addHeartRate: Int) {
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
                    if(d == data.recordMesgs[data.recordMesgs.size - 1]){
                        newDateTime.add(data.sessionMesgs[0].totalElapsedTime.toLong())
                    }
                    d.timestamp = newDateTime
                }
                d.enhancedSpeed += addSpeed
                d.heartRate = (d.heartRate + addHeartRate).toShort()
            }
        }
    }
}