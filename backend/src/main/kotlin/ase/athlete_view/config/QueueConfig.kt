package ase.athlete_view.config

import ase.athlete_view.domain.csp.util.QueueResponseListener
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class QueueConfig(private val objectMapper: ObjectMapper,
                  @Value("\${worker.rabbitmq.request_queue}") private val rmqRequestQueue: String,
                  @Value("\${worker.rabbitmq.response_queue}") private val rmqResponseQueue: String) {
    val log = KotlinLogging.logger {}

    @Bean
    fun queueIn(): Queue {
        log.trace { "Config | queueIn()" }
        return Queue(rmqResponseQueue, false)
    }

    @Bean
    fun queueOut(): Queue {
        log.trace { "Config | queueOut()" }
        return Queue(rmqRequestQueue, false)
    }

    @Bean
    fun container(connectionFactory: ConnectionFactory?,
                  listenerAdapter: MessageListenerAdapter?): SimpleMessageListenerContainer {
        log.trace { "Config | container()" }
        val container = SimpleMessageListenerContainer()
        container.connectionFactory = connectionFactory!!
        container.setQueueNames(rmqResponseQueue)
        container.setMessageListener(listenerAdapter!!)
        return container
    }

    @Bean
    fun listenerAdapter(receiver: QueueResponseListener?): MessageListenerAdapter {
        log.trace { "Config | listenerAdapter()" }
        return MessageListenerAdapter(receiver, "receiveMessage")
    }

    @Bean
    fun amqpTemplate(connectionFactory: ConnectionFactory): AmqpTemplate {
        log.trace { "Config | amqpTemplate()" }
        val temp = RabbitTemplate(connectionFactory)
        val jsonMessageConverter = Jackson2JsonMessageConverter(objectMapper)
        temp.messageConverter = jsonMessageConverter
        return temp
    }


}