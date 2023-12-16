package ase.athlete_view.domain.user.service.impl

import ase.athlete_view.common.exception.entity.ConflictException
import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.domain.authentication.service.AuthValidationService
import ase.athlete_view.domain.user.persistence.PreferencesRepository
import ase.athlete_view.domain.user.persistence.UserRepository
import ase.athlete_view.domain.user.pojo.dto.AthleteDTO
import ase.athlete_view.domain.user.pojo.dto.PreferencesDTO
import ase.athlete_view.domain.user.pojo.dto.TrainerDTO
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import ase.athlete_view.domain.user.pojo.entity.Athlete
import ase.athlete_view.domain.user.pojo.entity.Preferences
import ase.athlete_view.domain.user.pojo.entity.Trainer
import ase.athlete_view.domain.user.pojo.entity.User
import ase.athlete_view.domain.user.service.UserService
import ase.athlete_view.domain.user.service.mapper.UserMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserServiceImpl(private val userRepository: UserRepository,
                      private val validationService: AuthValidationService,
                      private val preferencesRepository: PreferencesRepository,
    private val userMapper: UserMapper) : UserService {
    val log = KotlinLogging.logger {}

    @Transactional
    override fun save(user: User): User {
        log.trace { "save ${user.email}" }
        // store new preferences object if not already present
        if (user.preferences == null) {
            val genericPreferences = Preferences(user.id);
            user.preferences = preferencesRepository.save(genericPreferences);
        }
        return this.userRepository.save(user)
    }

    @Transactional
    override fun saveAll(users: List<User>): List<User> {
        log.trace { "saveAll ${users.map { user: User -> user.email }}" }
        users.iterator().forEach { user ->
            if (user.preferences == null) {
               user.preferences = preferencesRepository.save(Preferences(user.id));
            }
        }
        return this.userRepository.saveAll(users)
    }

    override fun getByEmail(email: String): User {
        log.trace { "getByEmail $email" }
        return this.userRepository.findByEmail(email) ?: throw NotFoundException("Could not find user by given email")
    }

    override fun getById(id: Long): User {
        log.trace { "getById $id" }
        return this.userRepository.findByIdOrNull(id) ?: throw NotFoundException("Could not find user by given id")
    }

    @Transactional
    override fun updateTrainer(trainerDTO: TrainerDTO) {
        log.trace { "updateTrainer ${trainerDTO.email}" }
        val trainer = this.userRepository.findByEmail(trainerDTO.email)
        if (trainer is Trainer){
            this.userMapper.toEntity(trainer, trainerDTO)
            this.validationService.validateUser(trainer)
            this.save(trainer)
        } else{
            throw ConflictException("Could not update trainer by being the athlete")
        }
    }

    @Transactional
    override fun updateAthlete(athleteDTO: AthleteDTO) {
        log.trace { "updateAthlete ${athleteDTO.email}" }
        val athlete = this.userRepository.findByEmail(athleteDTO.email)
        if (athlete is Athlete){
            this.userMapper.toEntity(athlete, athleteDTO)
            this.validationService.validateUser(athlete)
            this.save(athlete)
        } else{
            throw ConflictException("Could not update athlete by being the trainer")
        }
    }

    override fun getPreferences(userDTO: UserDTO): Preferences? {
        log.trace { "getPreferences ${userDTO.email}" }
        if (userDTO.id != null) {
            val user = this.userRepository.findById(userDTO.id!!);
            if (user.isPresent) {
                return user.get().preferences
            }
        }
        return null
    }

    override fun patchPreferences(userDTO: UserDTO, preferencesDTO: PreferencesDTO): Preferences? {
        log.trace { "patchPreferences ${userDTO.email}, $preferencesDTO" }
        if (userDTO.id != null) {
            val user = this.userRepository.findById(userDTO.id!!);
            if (user.isPresent) {
                // get user object and its preferences
                val userObj = user.get()
                val oldPreferences = userObj.preferences;

                // update the preferences using the method in the mapper
                oldPreferences?.emailNotifications = preferencesDTO.emailNotifications

                val newPreferences = this.preferencesRepository.save(oldPreferences!!)
                userObj.preferences = newPreferences
                this.userRepository.save(userObj)
                return newPreferences;
            }
        }
        return null
    }
}
