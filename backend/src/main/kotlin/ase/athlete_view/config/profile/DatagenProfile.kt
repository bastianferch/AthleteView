package ase.athlete_view.config.profile

import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.domain.user.service.UserService
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
@Profile("datagen")
class DatagenProfile(private val userService: UserService) {
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
        athlete.isConfirmed = true
        this.userService.save(athlete)

        val trainer = Trainer(
            1,
            "t@t",
            "trainer",
            BCryptPasswordEncoder().encode("tttttttt"),
            "Austria",
            "1030",
            "ABGVA",
            listOf(athlete)
        )
        trainer.isConfirmed = true
        this.userService.save(trainer)
    }
}
