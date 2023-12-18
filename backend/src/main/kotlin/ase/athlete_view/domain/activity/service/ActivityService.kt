package ase.athlete_view.domain.activity.service

import ase.athlete_view.domain.activity.pojo.entity.Interval
import ase.athlete_view.domain.activity.pojo.entity.Activity
import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.user.pojo.entity.User
import com.garmin.fit.FitMessages
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

interface ActivityService {
    fun createPlannedActivity(plannedActivity: PlannedActivity, userId: Long): PlannedActivity

    fun getPlannedActivity(id: Long, userId: Long): PlannedActivity

    fun getAllPlannedActivities(userId: Long, startDate: LocalDateTime?, endDate: LocalDateTime?): List<PlannedActivity>

    fun updatePlannedActivity(id: Long, plannedActivity: PlannedActivity, userId: Long): PlannedActivity

    fun importActivity(files: List<MultipartFile>, userId: Long): Activity

    fun getAllActivities(uid: Long, startDate: LocalDateTime?, endDate: LocalDateTime?): List<Activity>

    fun createInterval(interval: Interval): Interval
    fun calculateStats(data: FitMessages, user: User, item: MultipartFile): Pair<Activity, String>
}
