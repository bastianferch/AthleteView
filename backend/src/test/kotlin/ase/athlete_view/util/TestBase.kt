package ase.athlete_view.util

import ase.athlete_view.domain.user.persistence.UserRepository
import ase.athlete_view.domain.user.pojo.entity.User
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
// doesn't work if u extend/implement it. fun :)
class TestBase {
    @Autowired
    private lateinit var ur: UserRepository

    @BeforeEach
    fun setup() {
        val u = User(null, "a@s.com", "Josef", "asdf", "Austria", "1337")
//        userRepository.save(u)
        ur.save(u)
    }
}