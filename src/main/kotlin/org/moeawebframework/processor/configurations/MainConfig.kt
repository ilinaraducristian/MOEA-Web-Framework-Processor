package org.moeawebframework.processor.configurations

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

var cdn_url = ""

@Configuration
class MainConfig {

  companion object {

    fun getFromCDN(sha256: String): Mono<ClientResponse> {
      return WebClient.create("""$cdn_url/$sha256""").get().exchange()
    }

  }

  @Autowired
  fun setCDNUrl(@Value("\${cdn_url}") CDNUrl: String) {
    cdn_url = CDNUrl
  }

  @Bean
  fun executorService(): ExecutorService {
    return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
  }

  @Bean
  fun objectMapper(): ObjectMapper {
    return ObjectMapper()
  }

}