package ase.athlete_view.domain.user.persistence

import ase.athlete_view.domain.user.pojo.entity.Athlete
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface AthleteRepository : JpaRepository<Athlete, Long> {
    @Query("SELECT a FROM Athlete a WHERE a.trainer.id = :trainerId")
    fun findAllByTrainerId(@Param("trainerId") trainerId:Long): List<Athlete>

}
