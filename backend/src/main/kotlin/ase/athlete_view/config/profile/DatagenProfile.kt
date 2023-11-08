package ase.athlete_view.config.profile

import ase.athlete_view.domain.user.pojo.entity.User
import ase.athlete_view.domain.user.service.UserService
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("datagen")
class DatagenProfile(private val userService: UserService)  {
    @PostConstruct
    fun init() {
        // todo change password to bcrypt version.
        val user = User(0, "a@a", "test name v57", "a")
        this.userService.save(user)
    }
}