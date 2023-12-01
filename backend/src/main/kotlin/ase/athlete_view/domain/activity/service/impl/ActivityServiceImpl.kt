package ase.athlete_view.domain.activity.service.impl

import ase.athlete_view.domain.activity.persistence.ActivityRepository
import ase.athlete_view.domain.activity.persistence.FitDataRepositoryImpl
import ase.athlete_view.domain.activity.pojo.entity.Activity
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.activity.util.FitParser
import ase.athlete_view.domain.user.persistence.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import lombok.AllArgsConstructor
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
@AllArgsConstructor
class ActivityServiceImpl(
    val fitParser: FitParser,
    val activityRepo: ActivityRepository,
    val fitFileRepo: FitDataRepositoryImpl,
    val userRepository: UserRepository
): ActivityService {
    private val logger = KotlinLogging.logger {}

    @Transactional
    override fun importActivity(files: List<MultipartFile>, userId: Long): Unit {
        logger.debug { "Ready to parse ${files.size} (${files[0].name}) files for user w/ ID $userId" }

        val user = userRepository.findById(userId)
        if (!user.isPresent) {
            throw BadCredentialsException("User not found")
        }

        var ids = arrayOf<String>().toMutableList()
        for (item in files) {
            val data = fitParser.decode(item.inputStream)

            if (data.recordMesgs.size == 0) {
                continue // skip empty file
            }

            var powerSum = 0
            var hrSum = 0
            var calSum = 0
            var totalDistance = 0.0
            var cadenceSum = 0

            var hrMax: Short = 0
            var powerMax = 0

            for (d in data.recordMesgs) {
                val hr = d.heartRate ?: 0
                val dist = d.distance ?: 0.0f
                val power = d.power ?: 0
                val cal = d.calories ?: 0
                val cadence = d.cadence ?: 0

                if (hr > hrMax) {
                    hrMax = hr.toShort()
                }

                if (power > powerMax) {
                    powerMax = power
                }

                calSum += cal
                powerSum += power
                hrSum += hr
                totalDistance += dist
                cadenceSum += cadence
            }

            val totalElems = data.recordMesgs.size
            val avgBpm = hrSum / totalElems
            val avgPower = powerSum / totalElems
            val avgCadence = cadenceSum / totalElems

            val fitId: String = fitFileRepo.saveFitData(item)

            val activity = Activity(
                null,
                user.get(),
                0,
                avgBpm,
                hrMax.toInt(),
                totalDistance,
                calSum,
                avgCadence,
                avgPower,
                powerMax,
                0,
                1,
                fitId
            )

            ids.add(fitId)
            val respData = activityRepo.save(activity)
            logger.debug { respData.toString() }
        }
    }
}
