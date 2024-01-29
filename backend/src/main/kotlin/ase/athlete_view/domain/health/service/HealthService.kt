package ase.athlete_view.domain.health.service

import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.common.exception.entity.InternalException
import ase.athlete_view.domain.health.pojo.entity.Health
import ase.athlete_view.domain.user.pojo.entity.Athlete
import java.time.LocalDate

interface HealthService {
    /**
     * Calls the authenticated (see {@link GarminMock}) external API
     * to add a new health data to the current authenticated user.
     * If the health data already exists for the given day, does nothing.
     *
     * @param userId id of current authenticated user
     *
     * @throws InternalException when external API did not respond / sent the wrong data.
     * @throws NotFoundException when current authenticated user could not be fetched from db.
     */
    fun syncWithMockServer(userId: Long)

    /**
     * Provides all Health data from the current authenticated user.
     *
     * @param userId id of current authenticated user
     *
     * @throws NotFoundException when current authenticated user could not be fetched from db.
     */
    fun getAllByCurrentUser(userId: Long): List<Health>

    /**
     * Provides all Health data for a provided user between the provided time frames.
     *
     * @param userID is an id of the user.
     * @param start of a time interval.
     * @param end of a time interval.
     *
     * @apiNote This method is not authorized (should be done before).
     */
    fun getAllByUser(userID: Long, start: LocalDate, end: LocalDate): List<Health>

    /**
     * Provides all Health data from athlete if athlete allows data to be shared.
     *
     * @param athleteId id of athlete
     * @param userId id of current authenticated user
     *
     * @throws NotFoundException when current authenticated user could not be fetched from db.
     */
    fun getAllFromAthlete(athleteId: Long, userId: Long): List<Health>

    /**
     * Simple crud save.
     */
    fun save(health: Health): Health

    /**
     * Creates a random health data for the athlete for the last 7 days.
     * Athlete should not have the health data before.
     * This function should be used for DataGenerator only.
     *
     * @param athlete for which the health is created.
     */
    fun createHealthDataForTheLast30Days(athlete: Athlete)

}
