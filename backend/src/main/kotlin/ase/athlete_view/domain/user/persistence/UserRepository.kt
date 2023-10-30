package ase.athlete_view.domain.user.persistence

import ase.athlete_view.domain.user.pojo.entity.TestUser
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<TestUser, Long>
