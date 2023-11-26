package ase.athlete_view.domain.token.service

import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.domain.token.pojo.entity.EmailConfirmationToken
import ase.athlete_view.domain.token.pojo.entity.PasswordResetToken
import ase.athlete_view.domain.token.pojo.entity.TokenExpirationTime
import ase.athlete_view.domain.user.pojo.entity.User
import java.util.*

interface TokenService {
    /**
     * Creates an email confirmation token for a user with a specified expiration time.
     *
     * @param expirationTime after which token will become disabled.
     * There are weekly checks that delete the expired token @{link TokenScheduler}.
     * @param user who are connected to the token.
     */
    fun createEmailConfirmationToken(expirationTime: TokenExpirationTime, user: User): EmailConfirmationToken

    /**
     * Creates a password reset token for a user with a specified expiration time.
     *
     * @param expirationTime after which token will become disabled.
     * There are weekly checks that delete the expired token @{link TokenScheduler}.
     * @param user who are connected to the token.
     */
    fun createResetPasswordToken(expirationTime: TokenExpirationTime, user: User): PasswordResetToken


    /**
     * Deletes the token by its UUID.
     *
     * @param uuid of token
     */
    fun deleteToken(uuid: UUID)

    fun deleteAllRegistrationTokensByUser(userId: Long)
    fun deleteAllPasswordResetTokensByUser(userId: Long)

    /**
     * Provides a connected user.
     *
     * @param uuid of token
     * @throws NotFoundException if token could not be found.
     */
    fun getUserByToken(uuid: UUID): User

    /**
     * Deletes all tokens in db, which are expired.
     */
    fun deleteExpiredTokens()
}