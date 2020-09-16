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

  @MessageMapping("process")
  fun process(process: Mono<Process>): Mono<Unit> {
    println("ceva?")
    return process.flatMap {
      println("ce?")
      try {
        return@flatMap Mono.just(Processor(it))
      } catch (e: Exception) {
        return@flatMap Mono.error<Processor>(e)
      }
    }.doOnNext { processor ->
      println("ce2?")
      executorService.submit {
        println("1")
        try {
          println("2")
          processor.process()
          println("3")
          val newProcess = processor.newProcess
          newProcess.results = processor.getResults()
          println("4")
          newProcess.status = "processed"
          println("Process?1")
          if (newProcess.userId == null) {
            redisTemplate.opsForValue().set(newProcess.rabbitId, newProcess).block()
            println("Process?2")
          } else {
            processDAO.save(processor.newProcess).block()
            println("Process?3")
          }
          println("Process?4")
        } catch (e: Exception) {
          println("Process?5")
          processor.newProcess.status = "waiting"
          println("Exception in processor, it should never happen")
          e.printStackTrace()
        }
      }
    }
        .map { }
  }

  @MessageMapping("cancel")
  fun cancel(rabbitId: String): Mono<Unit> {
    return Mono.justOrEmpty(processors[rabbitId])
        .switchIfEmpty(Mono.error(RuntimeException(ProcessNotFoundException)))
        .map { it.cancel() }
  }

}
