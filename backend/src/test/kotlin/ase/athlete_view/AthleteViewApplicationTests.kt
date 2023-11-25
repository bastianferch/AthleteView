package ase.athlete_view

import ase.athlete_view.config.jwt.UserAuthProvider
import ase.athlete_view.domain.user.pojo.dto.UserDto
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import util.ContainerizedMongoTest

//@SpringBootTest
@ContainerizedMongoTest
class AthleteViewApplicationTests {
	@Test
	fun contextLoads() {
		assert(1==1)
	}
}
