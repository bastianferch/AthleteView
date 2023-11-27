package ase.athlete_view.domain.user.service.impl

import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.domain.user.persistence.UserRepository
import ase.athlete_view.domain.user.pojo.entity.User
import ase.athlete_view.domain.user.service.UserService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserServiceImpl(private val userRepository: UserRepository) : UserService {
    @Transactional
    override fun save(user: User): User {
        return this.userRepository.save(user)
    }

    @Transactional
    override fun saveAll(users: List<User>): List<User> {
        return this.userRepository.saveAll(users)
    }

    override fun getByEmail(string: String): User {
        return this.userRepository.findByEmail(string) ?: throw NotFoundException("Could not find user by given email")
    }

    override fun getById(id: Long): User {
        return this.userRepository.findByIdOrNull(id) ?: throw NotFoundException("Could not find user by given id")
    }
}
