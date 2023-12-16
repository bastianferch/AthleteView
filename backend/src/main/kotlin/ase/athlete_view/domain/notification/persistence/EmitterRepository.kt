package ase.athlete_view.domain.notification.persistence

import org.springframework.stereotype.Repository
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.*
import java.util.concurrent.ConcurrentHashMap

// TODO: this is in-memory. For production, convert this into an actual repo
@Repository
class EmitterRepository {
    private val userEmitterMap: MutableMap<Long, SseEmitter> = ConcurrentHashMap()

    fun save(id: Long, emitter: SseEmitter) {
        userEmitterMap[id] = emitter
    }

    fun deleteById(id: Long) {
        if (userEmitterMap.containsKey(id)) {
            userEmitterMap.remove(id)
        }
    }

    fun existsById(id: Long): Boolean {
        return userEmitterMap[id] != null
    }

    fun findById(id: Long): Optional<SseEmitter> {
        return Optional.ofNullable(userEmitterMap[id])
    }

    fun deleteAll() {
        this.userEmitterMap.clear()
    }
}