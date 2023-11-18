package ase.athlete_view.domain.csp.persistence

import ase.athlete_view.domain.csp.pojo.entity.Csp
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CspRepository : JpaRepository<Csp, Long> {
}
