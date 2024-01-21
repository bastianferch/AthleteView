package ase.athlete_view.domain.csp.util

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class QueueRequestSender(private val amqpTemplate: AmqpTemplate,
                         @Value("\${worker.rabbitmq.request_queue}") private val rmqRequestQueue: String) {

    private val logger = KotlinLogging.logger {}
    fun sendMessage(message: Any) {
        logger.debug { "Sending request through queue: $message" }
        amqpTemplate.convertAndSend(rmqRequestQueue, message)
    }
}