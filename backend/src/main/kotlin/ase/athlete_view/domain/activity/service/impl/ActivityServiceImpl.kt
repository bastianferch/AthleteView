package ase.athlete_view.domain.activity.service.impl

import ase.athlete_view.domain.activity.persistence.ActivityRepository
import ase.athlete_view.domain.activity.persistence.FitDataRepository
import ase.athlete_view.domain.activity.pojo.dto.FitData
import ase.athlete_view.domain.activity.pojo.entity.Activity
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.activity.util.FitParser
import lombok.AllArgsConstructor
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
@AllArgsConstructor
class ActivityServiceImpl(
    val fitParser: FitParser,
    val activityRepo: ActivityRepository,
    val fitFileRepo: FitDataRepository
): ActivityService {
    override fun importActivity(files: List<MultipartFile>) {
        println("================== PROCESSING FILES ==================")
        println("Total files found: " + files.size)

        for (item in files) {
            val data = fitParser.decode(item.inputStream)
            println("Data found: ")

            var powerSum = 0
            var hrSum = 0
            var calSum = 0
            var totalDistance = 0.0
            var cadenceSum = 0

            var hrMax: Short = 0
            var powerMax = 0

            // record mesg probably interesting
            for (d in data.recordMesgs) {
                val hr = d.heartRate ?: 0
                val dist = d.distance ?: 0.0f
                val spd = d.speed
                val time = d.timestamp
                val power = d.power ?: 0
                val vspd = d.verticalSpeed
                val lat = d.positionLat
                val long = d.positionLong
                val cal = d.calories ?: 0
                val cadence = d.cadence ?: 0

                if (hr > hrMax) {
                    hrMax = hr
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

            val respData = activityRepo.save(activity)
            println("Saving activity success.")
            println(respData)
        }

        println("================== PROCESSING COMPLETE ==================")
    }

    override fun getActivity(id: String): FitData? {
        return fitFileRepo.getFitData(id)
    }
}
