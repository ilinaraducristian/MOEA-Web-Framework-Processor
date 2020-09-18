package org.moeawebframework.processor

import org.moeawebframework.processor.configurations.redisType
import org.moeawebframework.processor.dao.ProcessDAO
import org.moeawebframework.processor.entities.Process
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.util.concurrent.ExecutorService

@Controller
class MainController(
    private val processDAO: ProcessDAO,
    private val redisTemplate: ReactiveRedisTemplate<String, redisType>,
    private val rabbitTemplate: RabbitTemplate,
    private val executorService: ExecutorService
) {

  @MessageMapping("process")
  fun process(process: Process) {
    val processor = Processor(rabbitTemplate, process)
    executorService.submit {
      try {
        processor.startProcessing()
        val newProcess = processor.process
        newProcess.results = processor.getResults()
        println(newProcess.results)
        newProcess.status = "processed"
        if (newProcess.userId == null) {
          redisTemplate.opsForValue().set(newProcess.rabbitId, newProcess).block()
        } else {
          processDAO.save(processor.process).block()
        }
      } catch (e: Exception) {
        processor.process.status = "waiting"
        println("Exception in processor, it should never happen")
        e.printStackTrace()
      }
    }
  }

  @MessageMapping("cancel")
  fun cancel(rabbitId: String) {
    if (processors[rabbitId] == null) throw RuntimeException(ProcessNotFoundException)
    processors[rabbitId]?.cancel()
  }

}
