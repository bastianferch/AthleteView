package ase.athlete_view.domain.user.service.impl

import ase.athlete_view.domain.user.persistence.UserRepository
import ase.athlete_view.domain.user.pojo.entity.TestUser
import ase.athlete_view.domain.user.service.UserService
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service

@Service
@RequiredArgsConstructor
class UserServiceImpl(private val userRepository: UserRepository) : UserService {
    override fun createTestUser(): TestUser {
        val user: TestUser = TestUser(0,"Testinio Camberbackendio");
        return userRepository.save(user)
    }
}
