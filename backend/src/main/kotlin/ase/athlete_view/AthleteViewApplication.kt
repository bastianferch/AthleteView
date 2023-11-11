package ase.athlete_view

import ase.athlete_view.domain.activity.persistence.ActivityRepository
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@SpringBootApplication
//@EnableMongoRepositories
//@EnableMongoRepositories(basePackageClasses = { ActivityRepository.class })
class AthleteViewApplication

fun main(args: Array<String>) {
	runApplication<AthleteViewApplication>(*args)
}
