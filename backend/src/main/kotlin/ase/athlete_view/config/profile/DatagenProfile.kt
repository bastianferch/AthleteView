package ase.athlete_view.config.profile

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
import java.time.LocalTime

@Component
@Profile("datagen")
class DatagenProfile(private val userService: UserService, private val tcService: TimeConstraintService)  {
    @PostConstruct
    fun init() {
        val athlete = Athlete(
            0,
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
            1,
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
    }
}
