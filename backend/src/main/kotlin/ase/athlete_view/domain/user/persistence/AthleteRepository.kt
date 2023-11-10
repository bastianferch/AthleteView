package ase.athlete_view.domain.user.persistence

import ase.athlete_view.domain.user.pojo.entity.Athlete
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AthleteRepository : JpaRepository<Athlete, Long> {}
