package ase.athlete_view.domain.authentication.service.impl

import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.config.jwt.UserAuthProvider
import ase.athlete_view.domain.authentication.dto.LoginDTO
import ase.athlete_view.domain.authentication.service.AuthenticationService
import ase.athlete_view.domain.user.pojo.dto.UserDto
import ase.athlete_view.domain.user.pojo.entity.User
import ase.athlete_view.domain.user.service.UserService
import ase.athlete_view.domain.user.service.mapper.UserMapper
import lombok.RequiredArgsConstructor
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Service
import java.lang.IllegalStateException

@Service
@RequiredArgsConstructor
class AuthenticationServiceImpl(
    private val userService: UserService,
    private val userMapper: UserMapper,
    private val userAuthProvider: UserAuthProvider) : AuthenticationService {
    override fun registerUser(user: User): User {
        // ToDo: validation / conflicts
        return this.userService.save(user)
    }

    override fun authenticateUser(loginDTO: LoginDTO): UserDto {
        try {
            val user = this.userService.getByEmail(loginDTO.email)
            // ToDo: use Bcrtypt to store and check hashes.
            if (user.password != loginDTO.password) {
                this.throwBadCredentialsException()
            }
            val dto = this.userMapper.toDTO(user)
            dto.token = this.userAuthProvider.createToken(dto)
            return dto
        } catch (e: NotFoundException) {
            this.throwBadCredentialsException()
        }
        // ToDo: how to circumvent writing this exception?
        throw IllegalStateException("By user authentication something went wrong")
    }

    private fun throwBadCredentialsException() {
        throw BadCredentialsException("Either email or password is incorrect")
    }
}
