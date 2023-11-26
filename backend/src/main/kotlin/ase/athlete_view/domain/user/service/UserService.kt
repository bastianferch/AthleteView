package ase.athlete_view.domain.user.service

import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.domain.user.pojo.entity.User

interface UserService {
    /**
     * Simple repository save.
     */
    fun save(user: User): User
    fun saveAll(users: List<User>): List<User>

    /**
     * Simple repository get.
     * @throws NotFoundException if user was not found.
     */
    fun getByEmail(string: String): User

    /**
     * Simple repository get.
     * @throws NotFoundException if user was not found.
     */
    fun getById(id: Long): User
}
