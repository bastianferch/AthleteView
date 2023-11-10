package ase.athlete_view.config.profile

import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.User
import ase.athlete_view.domain.user.service.UserService
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
@Profile("datagen")
class DatagenProfile(private val userService: UserService)  {
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
    }
}
