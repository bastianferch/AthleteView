package ase.athlete_view.util

import ase.athlete_view.domain.authentication.dto.LoginDTO
import ase.athlete_view.domain.user.pojo.dto.UserDto
import ase.athlete_view.domain.user.pojo.entity.User

class DefaultEntityCreatorUtil {
    private val defaultUserId: Long         = 1L
    private val defaultUserName: String     = "Testuser"
    private val defaultUserPassword: String = "password"
    private val defaultUserEmail: String    = "a@b.com"
    private val defaultUserZip: String      = "1337"
    private val defaultTokenValue: String   = "token"

    fun getUserDto(): UserDto {
        return UserDto(defaultUserId, defaultUserName, defaultUserEmail, defaultUserPassword, defaultTokenValue)
    }

    fun getUser(): User {
        return User(defaultUserId, defaultUserEmail, defaultUserName, defaultUserPassword, defaultUserZip, defaultTokenValue)
    }

    fun getLoginDto(): LoginDTO {
        return LoginDTO(defaultUserEmail, defaultUserPassword)
    }
}