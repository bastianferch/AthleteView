package ase.athlete_view.domain.csp.service

import ase.athlete_view.domain.csp.pojo.dto.CspDto
import ase.athlete_view.domain.csp.pojo.entity.CspJob

interface CspService {

    /**
     * Accepts a CSP Job, duplicates the template planned activities and pushes the job construct
     * into the queue for the worker. This is only possible for the next week (date automatically set)
     *
     * @param cspDto a mapping of athletes belonging to the trainer and their assigned activities,
     * together with whether the trainer has to be present
     * @param userId the id of the trainer
     * @return Unit
     */
    fun accept(cspDto: CspDto, userId: Long)

    /**
     * Reverts a CSP Job, deletes the assigned activities.
     * This is only possible for the next week (date automatically set)
     *
     * @param userId the id of the trainer
     * @return Unit
     */
    fun revertJob(userId: Long)

    /**
     * Gets the CSP Job for the trainer for next week (date automatically set)
     *
     * @param userId the id of the trainer
     * @return CspJob? object, which is null if there is no job yet or a CspJob object
     */
    fun getJob(userId: Long): CspJob?
}
