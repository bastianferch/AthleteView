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
package ase.athlete_view.domain.zone.service

import ase.athlete_view.domain.zone.pojo.dto.ZoneDto
import ase.athlete_view.domain.zone.pojo.entity.Zone

interface ZoneService {

    /**
     * returns all Zones for athlete with the given id
     */
    fun getAll(userId: Long): List<ZoneDto>

    /**
     * updates Zones for athlete with the given id
     */
    fun edit(userId: Long, zones: List<ZoneDto>): List<ZoneDto>

    /**
     * resets Zones for athlete with given id
     * will delete saved Zones and create new ones based on maximum heart rate of the athlete
     * maximum heart rate can be given as a parameter, otherwise calculated with a heuristic based on age
     */
    fun resetZones(userId: Long, maxHR: Int? = null): List<ZoneDto>

    /**
     * Creates a list of 5 Zones going from 50 to 100 percent of given maximum heart rate
     */
    fun calcZones(maxHR: Int): List<Zone>
}
