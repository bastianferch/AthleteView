package ase.athlete_view.config

import ase.athlete_view.domain.csp.util.QueueResponseListener
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class QueueConfig (private val objectMapper: ObjectMapper){
    companion object {
        const val QUEUE_IN = "athlete_view_response"
        const val QUEUE_OUT = "athlete_view_request"
    }

    @Bean
    fun queueIn(): Queue {
        return Queue(QUEUE_IN, false)
    }

    @Bean
    fun queueOut(): Queue {
        return Queue(QUEUE_OUT, false)
    }

    @Bean
    fun container(connectionFactory: ConnectionFactory?,
                  listenerAdapter: MessageListenerAdapter?): SimpleMessageListenerContainer {
        val container = SimpleMessageListenerContainer()
        container.connectionFactory = connectionFactory!!
        container.setQueueNames(QUEUE_IN)
        container.setMessageListener(listenerAdapter!!)
        return container
    }

    @Bean
    fun listenerAdapter(receiver: QueueResponseListener?): MessageListenerAdapter {
        return MessageListenerAdapter(receiver, "receiveMessage")
    }

    @Bean
    fun amqpTemplate(connectionFactory: ConnectionFactory): AmqpTemplate {
        val temp = RabbitTemplate(connectionFactory)
        val jsonMessageConverter = Jackson2JsonMessageConverter(objectMapper)
        temp.messageConverter = jsonMessageConverter
        return temp
    }


}