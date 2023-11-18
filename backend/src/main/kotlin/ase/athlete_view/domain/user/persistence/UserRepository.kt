package ase.athlete_view.domain.user.persistence

import ase.athlete_view.domain.user.pojo.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long>{
    fun findByEmail(email: String): User?
}
