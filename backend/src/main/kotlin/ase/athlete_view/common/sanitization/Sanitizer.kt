package ase.athlete_view.common.sanitization

import io.github.oshai.kotlinlogging.KotlinLogging
import ase.athlete_view.domain.activity.pojo.entity.Interval
import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.pojo.entity.Step
import ase.athlete_view.domain.authentication.dto.AthleteRegistrationDTO
import ase.athlete_view.domain.authentication.dto.TrainerRegistrationDTO
import ase.athlete_view.domain.notification.pojo.entity.Notification
import ase.athlete_view.domain.time_constraint.pojo.dto.DailyTimeConstraintDto
import ase.athlete_view.domain.time_constraint.pojo.dto.TimeConstraintDto
import ase.athlete_view.domain.time_constraint.pojo.dto.WeeklyTimeConstraintDto
import ase.athlete_view.domain.user.pojo.dto.AthleteDTO
import ase.athlete_view.domain.user.pojo.dto.TrainerDTO
import ase.athlete_view.domain.zone.pojo.dto.ZoneDto
import ase.athlete_view.domain.zone.pojo.entity.Zone
import org.owasp.html.PolicyFactory
import org.owasp.html.Sanitizers
import org.springframework.stereotype.Service

@Service
class Sanitizer {

    val log = KotlinLogging.logger {}

    fun sanitizeText(text: String): String {
        log.trace { "S | sanitizeText($text)" }
        // only allow non-dangerous tags
        val policy: PolicyFactory = Sanitizers.FORMATTING
        return policy.sanitize(text)
    }

    fun sanitizeNotification(notification: Notification): Notification {
        log.trace { "S | sanitizeNotification($notification)" }
        var newBody: String? = null
        if (notification.body != null) {
            newBody = sanitizeText(notification.body!!)
        }

        return Notification(
            notification.id,
            notification.recipient,
            notification.read,
            notification.timestamp,
            sanitizeText(notification.header),
            newBody,
            notification.link);
    }

    fun sanitizeAthleteRegistrationDTO(athleteDto: AthleteRegistrationDTO): AthleteRegistrationDTO {
        log.trace { "S | sanitizeAthleteRegistrationDTO($athleteDto)" }
        return athleteDto.copy(
            name = sanitizeText(athleteDto.name!!),
            country = athleteDto.country?.let { sanitizeText(it) },
            zip = athleteDto.zip?.let { sanitizeText(it) }
        )
    }

    fun sanitizeAthleteDto(athleteDTO: AthleteDTO): AthleteDTO {
        log.trace { "S | sanitizeAthleteDto($athleteDTO)" }
        return AthleteDTO(
            id = athleteDTO.id,
            email = athleteDTO.email,
            name = sanitizeText(athleteDTO.name),
            country = athleteDTO.country?.let { sanitizeText(it) },
            zip = athleteDTO.zip?.let { sanitizeText(it) },
            dob = athleteDTO.dob,
            height = athleteDTO.height,
            weight = athleteDTO.weight,
            trainer = athleteDTO.trainer,
            trainerToBe = athleteDTO.trainerToBe,
            token = athleteDTO.token,
            userType = athleteDTO.userType,
        )
    }

    fun sanitizeTrainerDTO(trainerDTO: TrainerDTO): TrainerDTO {
        log.trace { "S | sanitizeTrainerDTO($trainerDTO)" }
        return TrainerDTO(
            id = trainerDTO.id,
            email = trainerDTO.email,
            name = sanitizeText(trainerDTO.name),
            country = trainerDTO.country?.let { sanitizeText(it) },
            zip = trainerDTO.zip?.let { sanitizeText(it) },
            code = trainerDTO.code,
            token = trainerDTO.token,
            userType = trainerDTO.userType,
            athletes = trainerDTO.athletes,
            unacceptedAthletes = trainerDTO.unacceptedAthletes,
        )
    }

    fun sanitizeTrainerRegistrationDTO(trainerDTO: TrainerRegistrationDTO): TrainerRegistrationDTO {
        log.trace { "S | sanitizeTrainerRegistrationDTO($trainerDTO)" }
        return trainerDTO.copy(
            name = sanitizeText(trainerDTO.name!!),
            country = trainerDTO.country?.let { sanitizeText(it) },
            zip = trainerDTO.zip?.let { sanitizeText(it) }
        )
    }

    fun sanitizePlannedActivity(plannedActivity: PlannedActivity): PlannedActivity {
        log.trace { "S | sanitizePlannedActivity($plannedActivity)" }
        var newNote: String? = null;
        if (plannedActivity.note != null) {
            newNote = sanitizeText(plannedActivity.note!!)
        }

        return PlannedActivity(
            id = plannedActivity.id,
            name = sanitizeText(plannedActivity.name),
            type = plannedActivity.type,
            interval = plannedActivity.interval,
            withTrainer = plannedActivity.withTrainer,
            template = plannedActivity.template,
            note = newNote,
            date = plannedActivity.date,
            estimatedDuration = plannedActivity.estimatedDuration,
            load = plannedActivity.load,
            createdBy = plannedActivity.createdBy,
            createdFor = plannedActivity.createdFor,
            activity = plannedActivity.activity
        )
    }

    fun sanitizeStep(step: Step): Step {
        log.trace { "S | sanitizeStep($step)" }
        if (step.note == null) return step;
        val newStep = step.copy()
        newStep.note = sanitizeText(step.note!!)
        return newStep;
    }

    fun sanitizeTimeConstraintDto(timeConstraintDto: TimeConstraintDto): TimeConstraintDto {
        log.trace { "S | sanitizeTimeConstraintDto($timeConstraintDto)" }
        if (timeConstraintDto is DailyTimeConstraintDto) {
            return DailyTimeConstraintDto(
                id = timeConstraintDto.id,
                isBlacklist = timeConstraintDto.isBlacklist,
                title = sanitizeText(timeConstraintDto.title),
                startTime = timeConstraintDto.startTime,
                endTime = timeConstraintDto.endTime
            )
        } else if (timeConstraintDto is WeeklyTimeConstraintDto) {
            return WeeklyTimeConstraintDto(
                id = timeConstraintDto.id,
                isBlacklist = timeConstraintDto.isBlacklist,
                title = sanitizeText(timeConstraintDto.title),
                constraint = timeConstraintDto.constraint
            )
        } else {
            return TimeConstraintDto(
                id = timeConstraintDto.id,
                isBlacklist = timeConstraintDto.isBlacklist,
                title = sanitizeText(timeConstraintDto.title),
            )
        }
    }

    fun sanitizeZoneDTO(zoneDto: ZoneDto): ZoneDto {
        log.trace { "S | sanitizeZoneDTO($zoneDto)" }
        return ZoneDto(
            id = zoneDto.id,
            name = sanitizeText(zoneDto.name),
            fromBPM = zoneDto.fromBPM,
            toBPM = zoneDto.toBPM,
        )
    }
}