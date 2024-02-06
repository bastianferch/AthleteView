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
