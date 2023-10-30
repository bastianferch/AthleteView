package ase.athlete_view.domain.user.service

import ase.athlete_view.domain.user.pojo.entity.TestUser

interface UserService {
    fun createTestUser(): TestUser
}
