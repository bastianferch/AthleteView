package ase.athlete_view.config.profile

import ase.athlete_view.domain.time_constraint.pojo.dto.WeeklyTimeConstraintDto
import ase.athlete_view.domain.time_constraint.pojo.entity.TimeFrame
import ase.athlete_view.domain.time_constraint.service.TimeConstraintService
import ase.athlete_view.domain.user.pojo.dto.UserDto
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.service.UserService
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@Component
@Profile("datagen")
class DatagenProfile(private val userService: UserService, private val tcService: TimeConstraintService)  {
    @PostConstruct
    fun init() {
        // todo change password to bcrypt version.
        val user = Athlete(0,
            "a@a",
            "test name v57",
            "a",
            "Austria",
            "1050",
            LocalDate.now(),
            1.8,
            1f)
        this.userService.save(user)

        this.tcService.save(WeeklyTimeConstraintDto(null, true, "JFX Meeting", user,
            TimeFrame(DayOfWeek.MONDAY, LocalTime.of(19,0), LocalTime.of(20,0))),
            //maybetodo change 1 from one
            UserDto(1, "", "", null, null))
    }
}
