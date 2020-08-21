package org.moeawebframework.processor

import org.moeawebframework.processor.configurations.redisType
import org.moeawebframework.processor.dao.ProcessDAO
import org.moeawebframework.processor.entities.Process
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Mono
import java.util.concurrent.ExecutorService

@Controller
class MainController(
    private val processDAO: ProcessDAO,
    private val redisTemplate: ReactiveRedisTemplate<String, redisType>,
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
          val newProcess = it.newProcess
          newProcess.results = it.getResults()
          newProcess.status = "processed"
          if (newProcess.userId == null) {
            redisTemplate.opsForValue().set(newProcess.rabbitId, newProcess).block()
          } else {
            processDAO.save(it.newProcess).block()
          }
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
