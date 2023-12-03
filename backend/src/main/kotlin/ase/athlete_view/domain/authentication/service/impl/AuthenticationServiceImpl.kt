package ase.athlete_view.domain.authentication.service.impl

import ase.athlete_view.common.exception.entity.ConflictException
import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.config.jwt.UserAuthProvider
import ase.athlete_view.domain.authentication.dto.AthleteRegistrationDTO
import ase.athlete_view.domain.authentication.dto.LoginDTO
import ase.athlete_view.domain.authentication.dto.ResetPasswordDTO
import ase.athlete_view.domain.authentication.dto.TrainerRegistrationDTO
import ase.athlete_view.domain.authentication.service.AuthValidationService
import ase.athlete_view.domain.authentication.service.AuthenticationService
import ase.athlete_view.domain.mail.pojo.entity.Email
import ase.athlete_view.domain.mail.service.MailService
import ase.athlete_view.domain.token.pojo.entity.TokenExpirationTime
import ase.athlete_view.domain.token.service.TokenService
import ase.athlete_view.domain.user.pojo.dto.AthleteDTO
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.domain.user.pojo.entity.User
import ase.athlete_view.domain.user.service.TrainerService
import ase.athlete_view.domain.user.service.UserService
import ase.athlete_view.domain.user.service.mapper.UserMapper
import lombok.RequiredArgsConstructor
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.collections.ArrayList

@Service
@RequiredArgsConstructor
class AuthenticationServiceImpl(
    private val userService: UserService,
    private val userMapper: UserMapper,
    private val userAuthProvider: UserAuthProvider,
    private val tokenService: TokenService,
    private val mailService: MailService,
    private val authValidationService: AuthValidationService,
    private val trainerService: TrainerService
) : AuthenticationService {
    private val encoder: BCryptPasswordEncoder = BCryptPasswordEncoder()

    @Transactional
    override fun registerUser(user: User): User {
        try {
            userService.getByEmail(user.email)
            throw ConflictException("User with provided email already exists!")
        } catch (_: NotFoundException) {
        }
        this.authValidationService.validateUser(user)
        user.password = BCryptPasswordEncoder().encode(user.password)
        val persistedUser = this.userService.save(user)
        this.createConfirmationTokenToUser(persistedUser)
        return persistedUser
    }

    @Transactional
    override fun registerAthlete(dto: AthleteRegistrationDTO): User {
        this.authValidationService.validateAthleteDTO(dto)
        val athlete =  this.registerUser(this.userMapper.toEntity(dto))
        if (dto.code == null){
            return athlete
        }
        val trainer = this.trainerService.getByCode(dto.code!!) ?: return athlete
        trainer.athletes += athlete as Athlete
        athlete.trainer = trainer
        this.userService.saveAll(listOf(trainer, athlete))
        return athlete
    }

    @Transactional
    override fun registerTrainer(dto: TrainerRegistrationDTO): User {
        this.authValidationService.validateTrainerDTO(dto)
        while (true) {
            val code = UUID.randomUUID().toString().substring(0,5).replace('-',Random().nextInt().toChar())
            if (this.trainerService.getByCode(code) != null) {
                continue
            }
            return this.registerUser(this.userMapper.toEntity(dto, code, ArrayList()))
        }

    }

    @Transactional
    override fun confirmRegistration(uuid: UUID) {
        val user = this.tokenService.getUserByToken(uuid)
        user.isConfirmed = true
        this.userService.save(user)
        this.tokenService.deleteToken(uuid)
    }


    override fun createJwtToken(id: Long): String {
        val user = this.userMapper.toDTO(this.userService.getById(id))
        return this.userAuthProvider.createToken(user)
    }

    override fun authenticateUser(loginDTO: LoginDTO): UserDTO {
        try {
            val user = this.userService.getByEmail(loginDTO.email)
            if (!encoder.matches(loginDTO.password, user.password)) {
                this.throwBadCredentialsException()
            }
            if (!user.isConfirmed) {
                throw ConflictException("Please confirm your email.")
            }
            val dto:UserDTO = if (user is Athlete){
                this.userMapper.toDTO(user);
            } else{
                this.userMapper.toDTO(user as Trainer);
            }
            dto.token = this.userAuthProvider.createToken(dto)
            return dto
        } catch (e: NotFoundException) {
            this.throwBadCredentialsException()
        }
        // ToDo: how to circumvent writing this exception?
        throw IllegalStateException("By user authentication something went wrong")
    }

    @Transactional
    override fun sendNewConfirmationToken(loginDTO: LoginDTO) {
        try {
            this.authenticateUser(loginDTO)
        } catch (e: ConflictException) {
            this.createConfirmationTokenToUser(this.userService.getByEmail(loginDTO.email))
        }
    }

    @Transactional
    override fun forgotPassword(email: String) {
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
        val user = this.tokenService.getUserByToken(dto.token)
        user.password = BCryptPasswordEncoder().encode(dto.password)
        this.authValidationService.validateUser(user)
        this.userService.save(user)
        this.tokenService.deleteToken(dto.token)
    }


    @Transactional
    override fun createConfirmationTokenToUser(user: User) {
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
        throw BadCredentialsException("Either email or password is incorrect")
    }
}
