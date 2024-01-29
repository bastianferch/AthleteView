package ase.athlete_view.config.profile.datagen

import ase.athlete_view.domain.activity.persistence.PlannedActivityRepository
import ase.athlete_view.domain.activity.pojo.entity.Interval
import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.pojo.entity.Step
import ase.athlete_view.domain.activity.pojo.util.*
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.activity.util.FitParser
import ase.athlete_view.domain.user.pojo.entity.Athlete
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
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.stream.Collectors

// these variables are random numbers for the acceptance range, if you want a higher or lower precision change them
const val ACCEPTANCE_RANGE_IN_SECONDS = 60
const val ACCEPTANCE_RANGE_IN_BPM = 20

@Component
@Profile("datagen")
class ActivityDatagen(
    private val fitParser: FitParser,
    private val activityService: ActivityService,
    private val plannedActivityRepo: PlannedActivityRepository
) {
    var files = mutableListOf<MultipartFile>()

    var log = KotlinLogging.logger {}
    var faker = Faker()
    var dates = mutableListOf<LocalDateTime>()

    @PostConstruct
    fun init() {
        val currentDirectory = System.getProperty("user.dir")
        val dir: Path = if (currentDirectory.contains("backend")) {
            Paths.get("src/main/resources/fit-files/")
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

    fun createPlannedActivities(reduceSecondsPerKm: Int, athlete: Athlete?, trainer: User): Int {
        val randomOrder = DayOfWeek.values().toList().shuffled()
        dates = mutableListOf()
        for (i in 0..6) {
            dates.add(LocalDateTime.now().plusDays(4).with(TemporalAdjusters.previous(randomOrder[i])).withHour(faker.random.nextInt(6, 18)))
        }
        // Load MEDIUM
        createRunInterval6times1kmWith1kmRecovery(
            if (athlete != null) 240 - reduceSecondsPerKm else 360,
            trainer,
            athlete,
            if (athlete == null) null else dates[0]
        )
        // Load HIGH
        createRunInterval7times1kmWith2MinRecovery(
            if (athlete != null) 240 - reduceSecondsPerKm else 240,
            trainer,
            athlete,
            if (athlete == null) null else dates[1]
        )
        // Load MEDIUM
        createBike21MinTest(
            if (athlete != null) 120 - reduceSecondsPerKm else 150,
            trainer,
            athlete,
            if (athlete == null) null else dates[2]
        )
        // Load HIGH
        createBike90km(
            if (athlete != null) 120 - reduceSecondsPerKm else 100,
            trainer,
            athlete,
            if (athlete == null) null else dates[3]
        )

        // Load MEDIUM
        createSwim2h(
            if (athlete != null) 140 - reduceSecondsPerKm/3 else 155,
            trainer,
            athlete,
            if (athlete == null) null else dates[4]
        )
        // Load LOW
        createRun60Min(
            if (athlete != null) 500 - reduceSecondsPerKm else 500,
            trainer,
            athlete,
            if (athlete == null) null else dates[6]
        )
        if (athlete != null) {
            // Load MEDIUM
            createSwim2h(
                155 - reduceSecondsPerKm/3,
                trainer,
                athlete,
                dates[5]
            )
            return 6
        }
        return 5

    }


    /**
     * changes the files of the given athlete
     * @param addSpeed the speed that should be added to the files
     * @param addHeartRate the heart rate that should be added to the files
     * @param user the user to which the activity belongs
     */
    fun changeFiles(addSpeed: Float, addHeartRate: Int, user: User): Int {
        val minSize = minOf(files.size, dates.size)
        for (i in 0 until minSize) {
            val data = fitParser.decode(files[i].inputStream)
            for (d in data.recordMesgs) {
                if (d == data.recordMesgs[0] || d == data.recordMesgs[data.recordMesgs.size - 1]) {
                    val lastWeek = dates[i]
                    val newDateTime = DateTime(
                        lastWeek.withHour(dates[i].hour + 1).withMinute(dates[i].minute).withSecond(dates[i].second)
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

            activityService.calculateStats(data, user, modifyMultipartFile(files[i]))
        }
        return files.size
    }

    private fun modifyMultipartFile(originalFile: MultipartFile): MultipartFile {
        val byteArray = (faker.pokemon.names() + faker.harryPotter.spells() + faker.bojackHorseman.tongueTwisters()).toByteArray()


        val originalBytes = originalFile.bytes
        val combinedBytes = originalBytes //+ byteArray

        return MockMultipartFile(
                originalFile.name,
                originalFile.originalFilename,
                originalFile.contentType,
                combinedBytes
        )
    }

    private fun createRunInterval7times1kmWith2MinRecovery(targetPace: Int, createdBy: User, createdFor: Athlete?, date: LocalDateTime?) {
        var withTrainer = true
        if (createdFor == null) {
            withTrainer = false
        }
        val plannedActivity = PlannedActivity(
            null, " 7x(1km P:1')", ActivityType.RUN,
            Interval(
                null, 1, listOf(
                    Interval(
                        null, 1, null,
                        Step(null, StepType.WARMUP, StepDurationType.LAPBUTTON, null, null, null, null, null, null)
                    ),
                    Interval(
                        null, 7, listOf(
                            Interval(
                                null, 1, null,
                                Step(
                                    null,
                                    StepType.ACTIVE,
                                    StepDurationType.DISTANCE,
                                    1,
                                    StepDurationUnit.KM,
                                    StepTargetType.PACE,
                                    if (createdFor != null) targetPace - ACCEPTANCE_RANGE_IN_SECONDS else targetPace,
                                    if (createdFor != null) targetPace + ACCEPTANCE_RANGE_IN_SECONDS else targetPace + 20,
                                    ""
                                )
                            ),
                            Interval(
                                null, 1, null,
                                Step(null, StepType.RECOVERY, StepDurationType.TIME, 2, StepDurationUnit.MIN, null, null, null, null)
                            )
                        ),
                        null
                    ),
                    Interval(
                        null, 1, null,
                        Step(null, StepType.COOLDOWN, StepDurationType.LAPBUTTON, null, null, null, null, null, null)
                    )
                ), null
            ),
            withTrainer, !withTrainer, "", date, 90, Load.HIGH, createdBy, createdFor, null
        )
        plannedActivity.interval = activityService.createInterval(plannedActivity.interval)
        plannedActivityRepo.save(plannedActivity)
    }

    private fun createRunInterval6times1kmWith1kmRecovery(targetPace: Int, createdBy: User, createdFor: Athlete?, date: LocalDateTime?) {
        var withTrainer = true
        if (createdFor == null) {
            withTrainer = false
        }
        val plannedActivity = PlannedActivity(
            null, " 6x(1km P:1km)", ActivityType.RUN,
            Interval(
                null, 1, listOf(
                    Interval(
                        null, 1, null,
                        Step(null, StepType.WARMUP, StepDurationType.LAPBUTTON, null, null, null, null, null, null)
                    ),
                    Interval(
                        null, 6, listOf(
                            Interval(
                                null, 1, null,
                                Step(
                                    null,
                                    StepType.ACTIVE,
                                    StepDurationType.DISTANCE,
                                    1,
                                    StepDurationUnit.KM,
                                    StepTargetType.PACE,
                                    if (createdFor != null) targetPace - ACCEPTANCE_RANGE_IN_SECONDS else targetPace,
                                    if (createdFor != null) targetPace + ACCEPTANCE_RANGE_IN_SECONDS else targetPace + 20,
                                    ""
                                )
                            ),
                            Interval(
                                null, 1, null,
                                Step(null, StepType.RECOVERY, StepDurationType.DISTANCE, 1, StepDurationUnit.KM, null, null, null, null)
                            )
                        ),
                        null
                    ),
                    Interval(
                        null, 1, null,
                        Step(null, StepType.COOLDOWN, StepDurationType.LAPBUTTON, null, null, null, null, null, null)
                    )
                ), null
            ),
            withTrainer, !withTrainer, "", date, 105, Load.MEDIUM, createdBy, createdFor, null
        )

        plannedActivity.interval = activityService.createInterval(plannedActivity.interval)
        plannedActivityRepo.save(plannedActivity)
    }

    private fun createRun60Min(targetPace: Int, createdBy: User, createdFor: Athlete?, date: LocalDateTime?) {
        var withTrainer = true
        if (createdFor == null) {
            withTrainer = false
        }
        val plannedActivity = PlannedActivity(
            null, "60' ", ActivityType.RUN,
            Interval(
                null, 1, listOf(
                    Interval(
                        null, 1, listOf(
                            Interval(
                                null, 1, null,
                                Step(
                                    null,
                                    StepType.ACTIVE,
                                    StepDurationType.TIME,
                                    60,
                                    StepDurationUnit.MIN,
                                    StepTargetType.PACE,
                                    if (createdFor != null) targetPace - ACCEPTANCE_RANGE_IN_SECONDS else targetPace,
                                    if (createdFor != null) targetPace + ACCEPTANCE_RANGE_IN_SECONDS else targetPace + 20,
                                    ""
                                )
                            )
                        ),
                        null
                    ),
                ), null
            ),
            withTrainer, !withTrainer, "", date, 60, Load.LOW, createdBy, createdFor, null
        )
        plannedActivity.interval = activityService.createInterval(plannedActivity.interval)
        plannedActivityRepo.save(plannedActivity)
    }

    private fun createBike21MinTest(targetPace: Int, createdBy: User, createdFor: Athlete?, date: LocalDateTime?) {
        var withTrainer = true
        if (createdFor == null) {
            withTrainer = false
        }
        val plannedActivity = PlannedActivity(
            null, " 1x21'", ActivityType.BIKE,
            Interval(
                null, 1, listOf(
                    Interval(
                        null, 1, null,
                        Step(null, StepType.WARMUP, StepDurationType.LAPBUTTON, null, null, null, null, null, null)
                    ),
                    Interval(
                        null, 1, listOf(
                            Interval(
                                null, 1, null,
                                Step(
                                    null,
                                    StepType.ACTIVE,
                                    StepDurationType.TIME,
                                    21,
                                    StepDurationUnit.MIN,
                                    StepTargetType.PACE,
                                    if (createdFor != null) targetPace - ACCEPTANCE_RANGE_IN_SECONDS else targetPace,
                                    if (createdFor != null) targetPace + ACCEPTANCE_RANGE_IN_SECONDS else targetPace + 20,
                                    ""
                                )
                            )
                        ),
                        null
                    ),
                    Interval(
                        null, 1, null,
                        Step(null, StepType.COOLDOWN, StepDurationType.LAPBUTTON, null, null, null, null, null, null)
                    )
                ), null
            ),
            withTrainer, !withTrainer, "", date, 45, Load.MEDIUM, createdBy, createdFor, null
        )
        plannedActivity.interval = activityService.createInterval(plannedActivity.interval)
        plannedActivityRepo.save(plannedActivity)
    }

    private fun createBike90km(targetPace: Int, createdBy: User, createdFor: Athlete?, date: LocalDateTime?) {
        var withTrainer = true
        if (createdFor == null) {
            withTrainer = false
        }
        val plannedActivity = PlannedActivity(
            null, " 90km", ActivityType.BIKE,
            Interval(
                null, 1, listOf(
                    Interval(
                        null, 1, listOf(
                            Interval(
                                null, 1, null,
                                Step(
                                    null,
                                    StepType.ACTIVE,
                                    StepDurationType.DISTANCE,
                                    90,
                                    StepDurationUnit.KM,
                                    StepTargetType.PACE,
                                    if (createdFor != null) targetPace - ACCEPTANCE_RANGE_IN_SECONDS else targetPace,
                                    if (createdFor != null) targetPace + ACCEPTANCE_RANGE_IN_SECONDS else targetPace + 20,
                                    ""
                                )
                            )
                        ),
                        null
                    ),
                ), null
            ),
            withTrainer, !withTrainer, "", date, 225, Load.HIGH, createdBy, createdFor, null
        )
        plannedActivity.interval = activityService.createInterval(plannedActivity.interval)
        plannedActivityRepo.save(plannedActivity)
    }

    private fun createSwim2h(targetHeartRate: Int, createdBy: User, createdFor: Athlete?, date: LocalDateTime?) {
        var withTrainer = true
        if (createdFor == null) {
            withTrainer = false
        }
        val plannedActivity = PlannedActivity(
            null, "2h", ActivityType.SWIM,
            Interval(
                null, 1, listOf(
                    Interval(
                        null, 1, listOf(
                            Interval(
                                null, 1, null,
                                Step(
                                    null,
                                    StepType.ACTIVE,
                                    StepDurationType.TIME,
                                    120,
                                    StepDurationUnit.MIN,
                                    StepTargetType.HEARTRATE,
                                    if (createdFor != null) targetHeartRate - ACCEPTANCE_RANGE_IN_BPM else targetHeartRate,
                                    if (createdFor != null) targetHeartRate + ACCEPTANCE_RANGE_IN_BPM else targetHeartRate + 20,
                                    ""
                                )
                            )
                        ),
                        null
                    ),
                ), null
            ),
            withTrainer, !withTrainer, "", date, 120, Load.MEDIUM, createdBy, createdFor, null
        )
        plannedActivity.interval = activityService.createInterval(plannedActivity.interval)
        plannedActivityRepo.save(plannedActivity)
    }

}