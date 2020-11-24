package org.moeawebframework.processor.configs

import org.mockito.Mockito
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class RabbitMQTestConfig {

  @Bean
  fun rabbitTemplate(): RabbitTemplate {
    val rabbitTemplate = Mockito.mock(RabbitTemplate::class.java)
    Mockito.`when`(rabbitTemplate.convertAndSend(Mockito.any<String>(), Mockito.any<Any>()))
        .then {}
    return rabbitTemplate
  }

}