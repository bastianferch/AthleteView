package ase.athlete_view.domain.fitness.service

import ase.athlete_view.common.exception.entity.ForbiddenException

interface FitnessService {
    /**
     * Calculates the fitness of a target user.
     * Fitness is calculated by Health and Activity. If the Activity is not provided -> this day has less fitness.
     * The optimal fitness is between 70 and 90 %.
     *
     * @param byUser is who asks the data. Only the same athlete or his/her trainer can access this data.
     * @param targetUser is who the data belongs to.
     * @return the calculated fitness.
     * @throws ForbiddenException when user byUser has no access or targetUser is a trainer.
     */
    fun calculate(byUser: Long, targetUser: Long): List<Int>
}