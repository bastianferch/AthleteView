package ase.athlete_view.domain.csp.util

import ase.athlete_view.config.QueueConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.stereotype.Component

@Component
class QueueRequestSender(private val amqpTemplate: AmqpTemplate) {

    private val logger = KotlinLogging.logger {}
    fun sendMessage(message: Any) {
       logger.debug{"Sending request through queue: $message"}
        amqpTemplate.convertAndSend(QueueConfig.QUEUE_OUT, message);
    }
}