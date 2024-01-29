package ase.athlete_view.domain.zone.persistence

import ase.athlete_view.domain.user.pojo.entity.User
import ase.athlete_view.domain.zone.pojo.entity.Zone
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ZoneRepository: JpaRepository<Zone, Long> {

    fun findAllByUser(user: User): List<Zone>
    fun deleteByUser(user: User)

}