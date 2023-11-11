package ase.athlete_view.domain.activity.persistence

import ase.athlete_view.domain.activity.pojo.entity.Activity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.stereotype.Repository

// TODO: Fix Repo
@Repository
//@Configuration
interface ActivityRepository: MongoRepository<Activity, String> {
//    fun findAllById(id: String)
//    fun save(activity: Activity)
}
