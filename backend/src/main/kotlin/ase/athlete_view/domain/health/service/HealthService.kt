package ase.athlete_view.domain.health.service

import ase.athlete_view.common.exception.entity.ConflictException
import ase.athlete_view.common.exception.entity.InternalException

interface HealthService {
    /**
     * Calls the authenticated (see {@link GarminMock}) external API
     * to add a new health data to the current authenticated user.
     * If the health data already exists for the given day, does nothing.
     *
     * @throws InternalException when external API did not respond / sent the wrong data.
     * @throws ConflictException when current authenticated user could not be fetched from db.
     */
    fun mock()

}
