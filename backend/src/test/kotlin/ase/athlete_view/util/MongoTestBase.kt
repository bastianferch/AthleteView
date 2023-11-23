package ase.athlete_view.util

import org.springframework.context.annotation.Import


@Import(EmbeddedMongo::class)
class MongoTestBase: TestBase() {
}