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
package ase.athlete_view.domain.notification.persistence

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Repository
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Repository
class EmitterRepository {
    private val userEmitterMap: MutableMap<Long, SseEmitter> = ConcurrentHashMap()
    val log = KotlinLogging.logger {}

    fun save(id: Long, emitter: SseEmitter) {
        log.trace { "P | save($id)" }
        //delete old emitter
        deleteById(id)

        // set new one
        userEmitterMap[id] = emitter
    }

    fun deleteById(id: Long) {
        log.trace { "P | deleteById($id)" }
        if (userEmitterMap.containsKey(id)) {
            // do nothing on completion and close emitter
            userEmitterMap[id]?.onCompletion {}
            userEmitterMap[id]?.complete()
            userEmitterMap.remove(id)
        }
    }

    fun existsById(id: Long): Boolean {
        log.trace { "P | existsById($id)" }
        return userEmitterMap[id] != null
    }

    fun findById(id: Long): Optional<SseEmitter> {
        log.trace { "P | findById($id)" }
        return Optional.ofNullable(userEmitterMap[id])
    }

    fun deleteAll() {
        log.trace { "P | deleteAll()" }
        this.userEmitterMap.forEach { deleteById(it.key) }
    }
}
