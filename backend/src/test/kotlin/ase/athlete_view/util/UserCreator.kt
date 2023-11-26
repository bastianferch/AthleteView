package ase.athlete_view.util

import ase.athlete_view.domain.authentication.dto.LoginDTO
import ase.athlete_view.domain.user.pojo.dto.UserDto
import ase.athlete_view.domain.user.pojo.entity.User

class UserCreator {
    companion object {
        const val DEFAULT_USER_ID: Long         = 1L
        const val DEFAULT_USER_NAME: String     = "Testuser"
        const val DEFAULT_USER_PASSWORD: String = "password"
        const val DEFAULT_USER_EMAIL: String    = "a@b.com"
        const val DEFAULT_USER_ZIP: String      = "1337"
        const val DEFAULT_TOKEN_VALUE: String   = "token"
        const val DEFAULT_USER_COUNTRY: String  = "Austria"

        fun getUserDto(): UserDto {
            return UserDto(id = DEFAULT_USER_ID, name = DEFAULT_USER_NAME,
                email = DEFAULT_USER_EMAIL, password = DEFAULT_USER_PASSWORD,
                token = DEFAULT_TOKEN_VALUE)
        }

        fun getUser(): User {
            return User(id = DEFAULT_USER_ID, email = DEFAULT_USER_EMAIL,
                name = DEFAULT_USER_NAME, password = DEFAULT_USER_PASSWORD,
                country = DEFAULT_USER_COUNTRY, zip = DEFAULT_USER_ZIP)
        }

        fun getLoginDto(): LoginDTO {
            return LoginDTO(email = DEFAULT_USER_EMAIL, password = DEFAULT_USER_PASSWORD)
        }
    }
}