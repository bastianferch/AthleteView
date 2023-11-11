package ase.athlete_view.domain.activity.service.impl

import ase.athlete_view.domain.activity.persistence.ActivityRepository
import ase.athlete_view.domain.activity.pojo.entity.Activity
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.activity.util.FitParser
import lombok.AllArgsConstructor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
@AllArgsConstructor
class ActivityServiceImpl(
    val fitParser: FitParser,
//    @Autowired val activityRepo: ActivityRepository
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
                val hr = d.heartRate
                val dist = d.distance
                val spd = d.speed
                val time = d.timestamp
                val power = d.power
                val vspd = d.verticalSpeed
                val lat = d.positionLat
                val long = d.positionLong
                val cal = d.calories
                val cadence = d.cadence

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

            var totalElems = data.recordMesgs.size
            var avgBpm = hrSum / totalElems
            var avgPower = powerSum / totalElems
            var avgCadence = cadenceSum / totalElems

            var activity = Activity(
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
                1
            )

//            var respData = activityRepo.insert(activity)
            println("Saving activity success.")
//            println(respData)
        }

        println("================== PROCESSING COMPLETE ==================")
    }
}
