package ase.athlete_view.config.profile

import ase.athlete_view.domain.activity.persistence.PlannedActivityRepository
import ase.athlete_view.domain.activity.pojo.entity.Interval
import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.pojo.entity.Step
import ase.athlete_view.domain.activity.pojo.util.*
import ase.athlete_view.domain.activity.service.ActivityService
import ase.athlete_view.domain.time_constraint.pojo.dto.WeeklyTimeConstraintDto
import ase.athlete_view.domain.time_constraint.pojo.entity.TimeFrame
import ase.athlete_view.domain.time_constraint.service.TimeConstraintService
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.domain.user.service.UserService
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Component
@Profile("datagen")
class DatagenProfile(private val userService: UserService, private val tcService: TimeConstraintService, private val activityService: ActivityService, private val activityRepository: PlannedActivityRepository)  {
    @PostConstruct
    fun init() {
        val athlete = Athlete(
            1,
            "a@a",
            "athlete",
            BCryptPasswordEncoder().encode("aaaaaaaa"),
            "Austria",
            "1050",
            LocalDate.of(2000,1,1),
            1800,
            80000,
            null
        )
//        athlete.isConfirmed = true
        this.userService.save(athlete)

        val trainer = Trainer(
            2,
            "t@t",
            "trainer",
            BCryptPasswordEncoder().encode("tttttttt"),
            "Austria",
            "1030",
            "ABGVA",
            mutableSetOf(athlete)
        )
        trainer.isConfirmed = true
        this.userService.save(trainer)


        this.tcService.save(WeeklyTimeConstraintDto(null, true, "JFX Meeting", athlete,
            TimeFrame(DayOfWeek.MONDAY, LocalTime.of(19,0), LocalTime.of(20,0))),
            //maybetodo change 1 from one
            UserDTO(1, "", "", null, null, ""))


        val plannedActivity = PlannedActivity(1, ActivityType.RUN,
            Interval(null,1, listOf(
                Interval(null,1,null,
                    Step(null,StepType.WARMUP, StepDurationType.LAPBUTTON,null,null,null,null,null,null)),
                Interval(null,7, listOf(
                    Interval(null,1,null,
                        Step(null,StepType.ACTIVE, StepDurationType.DISTANCE,1,StepDurationUnit.KM,StepTargetType.PACE,240,260,"")),
                    Interval(null,1,null,
                        Step(null,StepType.RECOVERY,StepDurationType.TIME,2,StepDurationUnit.MIN,null,null,null,null))),
                    null),
                Interval(null,1,null,
                    Step(null,StepType.COOLDOWN,StepDurationType.LAPBUTTON,null,null,null,null,null,null))),null)
                    ,
            false,false, "", LocalDateTime.of(2023,9,30,12,10),trainer,athlete, null)

        activityService.createInterval(plannedActivity.interval)
        activityRepository.save(plannedActivity)

    }
}
