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
package ase.athlete_view.domain.authentication.service.impl

import ase.athlete_view.common.exception.entity.ConflictException
import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.common.sanitization.Sanitizer
import ase.athlete_view.config.jwt.UserAuthProvider
import ase.athlete_view.domain.authentication.dto.*
import ase.athlete_view.domain.authentication.service.AuthValidationService
import ase.athlete_view.domain.authentication.service.AuthService
import ase.athlete_view.domain.mail.pojo.entity.Email
import ase.athlete_view.domain.mail.service.MailService
import ase.athlete_view.domain.notification.service.NotificationService
import ase.athlete_view.domain.time_constraint.service.TimeConstraintService
import ase.athlete_view.domain.token.pojo.entity.TokenExpirationTime
import ase.athlete_view.domain.token.service.TokenService
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.domain.user.pojo.entity.User
import ase.athlete_view.domain.user.service.TrainerService
import ase.athlete_view.domain.user.service.UserService
import ase.athlete_view.domain.zone.service.ZoneService
import io.github.oshai.kotlinlogging.KotlinLogging
import lombok.RequiredArgsConstructor
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@RequiredArgsConstructor
class AuthServiceImpl(
    private val userService: UserService,
    private val userAuthProvider: UserAuthProvider,
    private val tokenService: TokenService,
    private val mailService: MailService,
    private val authValidationService: AuthValidationService,
    private val trainerService: TrainerService,
    private val timeConstraintService: TimeConstraintService,
    private val zoneService: ZoneService,
    private val notificationService: NotificationService,
    private val sanitizer: Sanitizer
) : AuthService {
    private val encoder: BCryptPasswordEncoder = BCryptPasswordEncoder()
    val log = KotlinLogging.logger {}

    @Transactional
    override fun registerUser(user: User): User {
        log.trace { "S | registerUser($user)" }
        try {
            userService.getByEmail(user.email)
            throw ConflictException("User with provided email already exists!")
        } catch (_: NotFoundException) {
        }
        this.authValidationService.validateUser(user)
        user.password = BCryptPasswordEncoder().encode(user.password)
        val persistedUser = this.userService.save(user)
        timeConstraintService.createDefaultTimeConstraintsForUser(persistedUser)
        this.createConfirmationTokenToUser(persistedUser)
        return persistedUser
    }

    @Transactional
    override fun registerAthlete(dto: AthleteRegistrationDTO): User {
        log.trace { "S | registerAthlete($dto)" }

        val sanitizedDto = sanitizer.sanitizeAthleteRegistrationDTO(dto)
        this.authValidationService.validateAthleteDTO(sanitizedDto)
        val athlete = this.registerUser(sanitizedDto.toEntity()) as Athlete
        this.zoneService.resetZones(athlete.id!!)
        if (sanitizedDto.code == null) {
            return athlete
        }
        val trainer = this.trainerService.getByCode(sanitizedDto.code!!) ?: return athlete
        trainer.unacceptedAthletes += athlete
        this.notificationService.sendNotification(trainer.id!!, "Athlete request", "Would you like to accept the athlete ${if (athlete.name.length > 40) athlete.name.substring(40) else athlete.name}", "action/acceptAthlete/${athlete.id}")
        this.userService.saveAll(listOf(trainer, athlete))
        return athlete
    }

    @Transactional
    override fun registerTrainer(dto: TrainerRegistrationDTO): User {
        log.trace { "S | registerTrainer($dto)" }

        val sanitizedDto = sanitizer.sanitizeTrainerRegistrationDTO(dto)
        this.authValidationService.validateTrainerDTO(sanitizedDto)
        while (true) {
            val code = UUID.randomUUID().toString().substring(0, 5).replace('-', Random().nextInt().toChar())
            if (this.trainerService.getByCode(code) != null) {
                continue
            }
            return this.registerUser(sanitizedDto.toEntity(code))
        }

    }

    @Transactional
    override fun confirmRegistration(uuid: UUID) {
        log.trace { "S | confirmRegistration($uuid)" }

        val user = this.tokenService.getUserByToken(uuid)
        user.isConfirmed = true
        this.userService.save(user)
        this.tokenService.deleteToken(uuid)
    }

    override fun createJwtToken(id: Long): String {
        log.trace { "S | createJwtToken($id)" }

        val user = this.userService.getById(id).toUserDTO()
        return this.userAuthProvider.createToken(user)
    }

    override fun authenticateUser(loginDTO: LoginDTO): UserDTO {
        log.trace { "S | authenticateUser($loginDTO)" }

        try {
            val user = this.userService.getByEmail(loginDTO.email)
            if (!encoder.matches(loginDTO.password, user.password)) {
                this.throwBadCredentialsException()
            }
            if (!user.isConfirmed) {
                throw ConflictException("Please confirm your email.")
            }
            val dto: UserDTO = when (user) {
                is Athlete -> {
                    user.toAthleteDto()
                }
                is Trainer -> {
                    user.toDto()
                }
                else -> {
                    throw IllegalStateException("User is neither athlete nor trainer")
                }
            }
            dto.token = this.userAuthProvider.createToken(dto)
            return dto
        } catch (e: NotFoundException) {
            this.throwBadCredentialsException()
        }
        throw IllegalStateException("By user authentication something went wrong")
    }

    @Transactional
    override fun sendNewConfirmationToken(loginDTO: LoginDTO) {
        log.trace { "S | sendNewConfirmationToken($loginDTO)" }

        try {
            this.authenticateUser(loginDTO)
        } catch (e: ConflictException) {
            this.createConfirmationTokenToUser(this.userService.getByEmail(loginDTO.email))
        }
    }

    @Transactional
    override fun forgotPassword(email: String) {
        log.trace { "S | forgotPassword($email)" }

        try {
            val user = this.userService.getByEmail(email)
            user.id?.let { this.tokenService.deleteAllPasswordResetTokensByUser(it) }
            val token = this.tokenService.createResetPasswordToken(TokenExpirationTime.TEN_MIN, user).uuid
            val mail = Email()
            mail.recipient = user.email
            mail.subject = "Reset email"
            // todo make env variable.
            mail.body = "Reset your password by entering the link: http://localhost:4200/auth/reset/$token"
            this.mailService.sendSimpleMail(mail)
        } catch (_: NotFoundException) {
        }
    }

    @Transactional
    override fun setNewPassword(dto: ResetPasswordDTO) {
        log.trace { "S | setNewPassword($dto)" }

        val user = this.tokenService.getUserByToken(dto.token)
        user.password = BCryptPasswordEncoder().encode(dto.password)
        this.authValidationService.validateUser(user)
        this.userService.save(user)
        this.tokenService.deleteToken(dto.token)
    }


    @Transactional
    override fun createConfirmationTokenToUser(user: User) {
        log.trace { "S | createConfirmationTokenToUser($user)" }

        user.id?.let { this.tokenService.deleteAllRegistrationTokensByUser(it) }
        val token = this.tokenService.createEmailConfirmationToken(TokenExpirationTime.ONE_HOUR, user).uuid
        val mail = Email()
        mail.recipient = user.email
        mail.subject = "Registration"
        // todo make env variable.
        mail.body = "Please confirm your account by entering the link: http://localhost:4200/auth/confirm/$token"
        this.mailService.sendSimpleMail(mail)
    }

    private fun throwBadCredentialsException() {
        log.trace { "S | throwBadCredentialsException()" }
        throw BadCredentialsException("Either email or password is incorrect")
    }
}
