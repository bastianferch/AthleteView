package ase.athlete_view.domain.token.service.impl

import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.domain.token.persistence.TokenRepository
import ase.athlete_view.domain.token.pojo.entity.EmailConfirmationToken
import ase.athlete_view.domain.token.pojo.entity.PasswordResetToken
import ase.athlete_view.domain.token.pojo.entity.TokenExpirationTime
import ase.athlete_view.domain.token.service.TokenService
import ase.athlete_view.domain.user.pojo.entity.User
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class TokenServiceImpl(private val repository: TokenRepository): TokenService {

    val log = KotlinLogging.logger {}

    @Transactional
    override fun createEmailConfirmationToken(expirationTime: TokenExpirationTime, user: User): EmailConfirmationToken {
        log.trace { "S | createEmailConfirmationToken($expirationTime, $user)" }
        val token = EmailConfirmationToken(
            null, expirationTime.expirationDate(), user
        )
        return this.repository.save(token)
    }

    @Transactional
    override fun createResetPasswordToken(expirationTime: TokenExpirationTime, user: User): PasswordResetToken {
        log.trace { "S | createResetPasswordToken($expirationTime, $user)" }
        val token = PasswordResetToken(
            null, expirationTime.expirationDate(), user
        )
        return this.repository.save(token)
    }
    @Transactional
    override fun deleteToken(uuid: UUID) {
        log.trace { "S | deleteToken($uuid)" }
        this.repository.deleteById(uuid)
    }

    override fun deleteAllRegistrationTokensByUser(userId: Long) {
        log.trace { "S | deleteAllRegistrationTokensByUser($userId)" }
        this.repository.deleteAllRegistrationTokensByUser(userId)
    }

    override fun deleteAllPasswordResetTokensByUser(userId: Long) {
        log.trace { "S | deleteAllPasswordResetTokensByUser($userId)" }
        this.repository.deleteAllPasswordResetTokensByUser(userId)
    }

    override fun getUserByToken(uuid: UUID): User {
        log.trace { "S | getUserByToken($uuid)" }
        val token = repository.findByUuid(uuid)
        if (token.isPresent){
            return token.get().user
        }
        throw NotFoundException("Could not find such a token.")
    }

    @Transactional
    override fun deleteExpiredTokens() {
        log.trace { "S | deleteExpiredTokens()" }
        repository.deleteExpired()
    }
}