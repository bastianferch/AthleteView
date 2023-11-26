package ase.athlete_view

import org.junit.jupiter.api.Test
import util.mongo.ContainerizedMongoTest

//@SpringBootTest
@ContainerizedMongoTest
class AthleteViewApplicationTests {
	@Test
	fun contextLoads() {
		assert(1==1)
	}
}
