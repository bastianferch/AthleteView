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
