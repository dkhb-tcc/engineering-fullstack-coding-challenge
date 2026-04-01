package com.example.demo.vitalsign.messaging

import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.core.Binding
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

const val VITAL_SIGN_QUEUE    = "vital-signs"
const val VITAL_SIGN_EXCHANGE = "vital-signs.exchange"
const val VITAL_SIGN_KEY      = "vital-signs.inbound"

@Configuration
@ConditionalOnProperty(name = ["rabbitmq.enabled"], havingValue = "true", matchIfMissing = false)
class VitalSignQueueConfig {
    @Bean fun vitalSignQueue()    = Queue(VITAL_SIGN_QUEUE, true)
    @Bean fun vitalSignExchange() = TopicExchange(VITAL_SIGN_EXCHANGE)
    @Bean fun vitalSignBinding(queue: Queue, exchange: TopicExchange): Binding =
        BindingBuilder.bind(queue).to(exchange).with(VITAL_SIGN_KEY)
    @Bean fun messageConverter() = Jackson2JsonMessageConverter()
}
