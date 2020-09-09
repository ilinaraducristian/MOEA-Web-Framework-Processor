package org.moeawebframework.processor.configurations

import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

var cdn_url = ""

@Configuration
class MainConfig {

  @Value("cdn_url")
  fun setcdn_url(cdnUri: String) {
    cdn_url = cdnUri
  }

  @Bean
  @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
  fun executorService(): ExecutorService {
    return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
  }

}

fun getFromCDN(sha256: String): Mono<ClientResponse> {
  return WebClient.create("$cdn_url/$sha256")
      .get()
      .exchange()
}