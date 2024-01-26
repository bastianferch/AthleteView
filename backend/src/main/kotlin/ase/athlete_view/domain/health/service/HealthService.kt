package ase.athlete_view.domain.health.service

import ase.athlete_view.common.exception.entity.NotFoundException
import ase.athlete_view.common.exception.entity.InternalException
import ase.athlete_view.domain.health.pojo.entity.Health

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

}
