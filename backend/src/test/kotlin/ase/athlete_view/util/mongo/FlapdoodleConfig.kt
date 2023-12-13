package ase.athlete_view.util.mongo

import de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@TestConfiguration
class Flapdoodle : EmbeddedMongoAutoConfiguration()

