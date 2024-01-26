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