package org.moeawebframework.processor

import org.moeawebframework.processor.dao.ProcessDAO
import org.moeawebframework.processor.entities.Process
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.concurrent.ExecutorService

@Controller
class MainController(
    private val processDAO: ProcessDAO,
    private val executorService: ExecutorService
) {

  @MessageMapping("processor")
  fun process(process: Mono<Process>): Mono<Unit> {
    return process.flatMap {
      try {
        return@flatMap Mono.just(Processor(it))
      } catch (e: Exception) {
        return@flatMap Mono.error<Processor>(e)
      }
    }.flatMap {
      executorService.submit {
        try {
          it.process()
          it.newProcess.results = it.getResults()
          it.newProcess.status = "processed"
          processDAO.save(it.newProcess).block()
        } catch (e: Exception) {
          it.newProcess.status = "waiting"
          println("Exception in processor, it should never happen:")
          e.printStackTrace()
        }
      }
      Mono.empty<Unit>()
    }
  }

}

fun getFromCDN(sha256: String): Mono<ClientResponse> {
  return WebClient.create("http://localhost:8070/$sha256")
      .get()
      .exchange()
}
