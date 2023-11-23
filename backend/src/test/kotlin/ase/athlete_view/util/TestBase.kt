package ase.athlete_view.util

import ase.athlete_view.domain.user.persistence.UserRepository
import ase.athlete_view.domain.user.pojo.entity.User
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class TestBase {
    @Autowired
    private lateinit var ur: UserRepository
    val logger = KotlinLogging.logger {}

    @BeforeEach
    fun setup(): Unit {
        logger.info { "Configuring test user" }
        val u = User(null, "a@s.com", "Josef", "asdf", "Austria", "1337")
        ur.save(u)
    }
}