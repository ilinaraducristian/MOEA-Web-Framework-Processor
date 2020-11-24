package org.moeawebframework.processor.configurations

import org.moeawebframework.processor.entities.QueueItem
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

typealias redisType = QueueItem

@Configuration
class RedisConfig {

  @Bean
  fun reactiveRedisTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, redisType> {
    val keySerializer = StringRedisSerializer()
    val valueSerializer: Jackson2JsonRedisSerializer<redisType> = Jackson2JsonRedisSerializer<redisType>(redisType::class.java)
    val builder: RedisSerializationContext.RedisSerializationContextBuilder<String, redisType> = RedisSerializationContext.newSerializationContext<String, redisType>(keySerializer)
    val context: RedisSerializationContext<String, redisType> = builder.value(valueSerializer).build()
    return ReactiveRedisTemplate<String, redisType>(factory, context)
  }

}