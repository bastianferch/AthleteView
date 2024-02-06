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
package ase.athlete_view.domain.activity.persistence

import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.activity.pojo.util.ActivityType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface PlannedActivityRepository : JpaRepository<PlannedActivity, Long> {


    @Query("SELECT p FROM PlannedActivity p WHERE p.createdFor.id = :userId AND p.type = :activityType AND :startDate <= p.date AND p.date <= :endDate  AND p.activity IS NULL")
    fun findActivitiesByUserIdTypeAndDateWithoutActivity(@Param("userId") userId: Long, @Param("activityType") activityType: ActivityType, @Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): List<PlannedActivity>

    fun findAllByCreatedForId(uid: Long): List<PlannedActivity>

    @Query("SELECT p FROM PlannedActivity p WHERE p.createdBy.id = :uid AND p.template = true")
    fun findAllTemplatesForUid(uid:Long): List<PlannedActivity>
}
