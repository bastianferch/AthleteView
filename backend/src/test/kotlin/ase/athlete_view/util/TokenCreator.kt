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
