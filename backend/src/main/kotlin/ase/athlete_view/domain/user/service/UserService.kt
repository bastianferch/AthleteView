package ase.athlete_view.domain.user.service

import ase.athlete_view.domain.user.pojo.entity.User

interface UserService {
    fun save(user: User): User
    fun getByEmail(string: String): User
}
