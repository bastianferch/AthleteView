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
