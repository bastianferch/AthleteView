/*-
 * #%L
 * athlete_view
 * %%
 * Copyright (C) 2023 - 2024 TU Wien INSO ASE GROUP 5 WS2023
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
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
