package ase.athlete_view.domain.authentication.service

import ase.athlete_view.common.exception.entity.ConflictException
import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.common.exception.entity.ValidationException
import ase.athlete_view.domain.authentication.dto.AthleteRegistrationDTO
import ase.athlete_view.domain.authentication.dto.LoginDTO
import ase.athlete_view.domain.authentication.dto.ResetPasswordDTO
import ase.athlete_view.domain.authentication.dto.TrainerRegistrationDTO
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import ase.athlete_view.domain.user.pojo.entity.User
import org.springframework.security.authentication.BadCredentialsException
import java.util.*

interface AuthenticationService {
    /**
     * Validates the given user (e.x. if the email already exists)
     * Encrypts the password with the BCrypt and saves the entity.
     * Sends a user notification to confirm the users' email.
     *
     * @param user to register.
     * @return user with a new ID.
     * @throws ConflictException if provided email already persisted.
     * @throws ValidationException if {@link AuthValidationService#validateUser} finds errors.
     */
    fun registerUser(user: User): User

    /**
     * Validates specifically the athlete. It is done to make sure,
     * that the user does not get a null-pointer by sending the dto with required field in dto.
     * Afterward calls the {@link registerUser}.
     *
     * @param dto with an Athlete
     * @throws ConflictException if provided email already persisted.
     * @throws ValidationException if {@link AuthValidationService#validateUser or #validateAthleteDTO} finds errors.
     */
    fun registerAthlete(dto: AthleteRegistrationDTO): User

    /**
     * Validates specifically the trainer. It is done to make sure,
     * that the user does not get a null-pointer by sending the dto with required field in dto.
     * Afterward calls the {@link registerUser}.
     *
     * @param dto with an Athlete
     * @throws ConflictException if provided email already persisted.
     * @throws ValidationException if {@link AuthValidationService#validateUser or #validateTrainerDTO} finds errors.
     */
    fun registerTrainer(dto: TrainerRegistrationDTO): User

    /**
     * Confirms the user's registration by a token, which was sent per email before.
     * Deletes the token.
     *
     * @param uuid of {@link Token}.
     * @throws NotFoundException if token was not found.
     */
    fun confirmRegistration(uuid: UUID)

    /**
     * Creates a jwt session token.
     *
     * @param id of user to create for.
     * @throws NotFoundException if user was not found.
     */
    fun createJwtToken(id: Long): String

    /**
     * Checks if the user/pass match and if the user account is confirmed (by email).
     *
     * @param loginDTO with email/pass.
     * @throws BadCredentialsException if user could not be found or the pass does not match.
     * @throws IllegalStateException should be impossible, probably bug in the kotlin.
     */
    fun authenticateUser(loginDTO: LoginDTO): UserDTO

    /**
     * Sends the user a new email with the confirmation token.
     *
     * @param loginDTO to authenticate the user.
     */
    fun sendNewConfirmationToken(loginDTO: LoginDTO)

    /**
     * After user clicked reset password,
     * sends an email with a new generated token.
     * This token will be needed to authorize the password change request.
     * All previous forgot password tokens are deleted.
     *
     * @param email of account owner (hopefully :))
     * If such email exists in db-> sends to the account owner an email to reset the password.
     */
    fun forgotPassword(email: String)

    /**
     * Sets the new password with a token, which was sent per email previously.
     *
     * @param dto with a new password and the token,
     * @throws ValidationException if password does not match the validation criteria (e.x. length must be higher than 8)
     * @throws NotFoundException if token is not persisted in db.
     */
    fun setNewPassword(dto: ResetPasswordDTO)

    /**
     * After user registration, user has to prove the identity.
     * Creates a token for user.
     * Sends a confirmation link with a token to the users' email.
     * All previous confirmation link tokens are deleted.
     */
    fun createConfirmationTokenToUser(user: User)
}
