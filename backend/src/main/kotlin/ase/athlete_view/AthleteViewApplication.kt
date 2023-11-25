package ase.athlete_view

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
//@EnableMongoRepositories
//@EnableMongoRepositories(basePackageClasses = [ FitDataRepository::class ])
class AthleteViewApplication

fun main(args: Array<String>) {
	runApplication<AthleteViewApplication>(*args)
}
