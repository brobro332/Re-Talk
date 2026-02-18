package xyz.re_talk.global.config

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.SimpleMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import xyz.re_talk.global.common.filter.TraceIdFilter

@Configuration
class RabbitMqConfig {
    companion object {
        const val EXCHANGE = "chat-exchange"
        const val QUEUE    = "chat-queue"
        const val ROUTING_KEY   = "chat-routing-key"
    }

    @Bean fun chatQueue() = Queue(QUEUE, true)
    @Bean fun chatExchange() = DirectExchange(EXCHANGE)
    @Bean fun chatBinding(chatQueue: Queue, chatExchange: DirectExchange): Binding =
        BindingBuilder.bind(chatQueue).to(chatExchange).with(ROUTING_KEY)

    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate {
        val template = RabbitTemplate(connectionFactory)
        template.messageConverter = SimpleMessageConverter()

        // 모든 메시지에 traceId 자동 추가
        template.setBeforePublishPostProcessors(
            org.springframework.amqp.core.MessagePostProcessor { message ->
                val traceId = TraceIdFilter.getCurrentTraceId()
                message.messageProperties.setHeader(TraceIdFilter.TRACE_ID_KEY, traceId)
                message
            }
        )

        return template
    }
}