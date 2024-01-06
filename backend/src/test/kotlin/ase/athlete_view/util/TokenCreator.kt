package ase.athlete_view.util

import ase.athlete_view.domain.token.pojo.entity.EmailConfirmationToken
import ase.athlete_view.domain.token.pojo.entity.PasswordResetToken
import ase.athlete_view.domain.token.pojo.entity.TokenExpirationTime
import ase.athlete_view.domain.user.pojo.entity.User
import java.util.*

class TokenCreator {
    companion object {
        val DEFAULT_EMAIL_CONFIRMATION_TOKEN_UUID: UUID = UUID.fromString("f668d2d8-1a10-44ea-b4bb-7f0d02989357")
        val DEFAULT_PASSWORD_RESET_TOKEN_UUID: UUID = UUID.fromString("253fe2f7-6434-4fc4-a6c8-29ec7b72c891")
        val DEFAULT_TOKEN_EXPIRY_DATE_IN_1_HOUR: Date = TokenExpirationTime.ONE_HOUR.expirationDate()
        val DEFAULT_TOKEN_USER: User = UserCreator.getAthlete(null)

        fun getDefaultEmailConfirmationToken(): EmailConfirmationToken{
            return EmailConfirmationToken(DEFAULT_EMAIL_CONFIRMATION_TOKEN_UUID, DEFAULT_TOKEN_EXPIRY_DATE_IN_1_HOUR, DEFAULT_TOKEN_USER)
        }

        fun getDefaultPasswordResetToken(): PasswordResetToken{
            return PasswordResetToken(DEFAULT_PASSWORD_RESET_TOKEN_UUID, DEFAULT_TOKEN_EXPIRY_DATE_IN_1_HOUR, DEFAULT_TOKEN_USER)
        }
    }
}