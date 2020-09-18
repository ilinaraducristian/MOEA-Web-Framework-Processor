package org.moeawebframework.processor.configurations

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Configuration
class MainConfig {

  @Bean
  fun executorService(): ExecutorService {
    return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
  }

  @Bean
  fun objectMapper(): ObjectMapper {
    return ObjectMapper()
  }

}

fun getFromCDN(sha256: String): Mono<ClientResponse> {
  return WebClient.create("localhost:8280/$sha256")
      .get()
      .exchange()
}