package org.moeawebframework.processor.controllers

import kotlinx.coroutines.runBlocking
import org.moeawebframework.processor.Processor
import org.moeawebframework.processor.QueueItemNotFoundException
import org.moeawebframework.processor.RedisAdapter
import org.moeawebframework.processor.entities.QueueItem
import org.moeawebframework.processor.processors
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.util.concurrent.ExecutorService

@Controller
class PublicController(
    private val redisAdapter: RedisAdapter,
    private val rabbitTemplate: RabbitTemplate,
    private val executorService: ExecutorService
) {

  @MessageMapping("startProcessing")
  fun startProcessing(queueItem: QueueItem) {
    val processor = Processor(rabbitTemplate, queueItem)
    executorService.submit {
      try {
        processor.startProcessing()
        val newProcess = processor.queueItem
        newProcess.results = processor.getResults()
        newProcess.status = "processed"
        runBlocking {
          redisAdapter.set(newProcess.rabbitId, newProcess)
        }
      } catch (e: Exception) {
        processor.queueItem.status = "waiting"
        runBlocking {
          redisAdapter.set(processor.queueItem.rabbitId, queueItem)
        }
        println("Exception in processor, it should never happen")
        e.printStackTrace()
      }
    }
  }

  @MessageMapping("cancelProcessing")
  suspend fun cancelProcessing(rabbitId: String) {
    if (processors[rabbitId] == null) throw RuntimeException(QueueItemNotFoundException)
    processors[rabbitId]?.cancel()
    val queueItem = redisAdapter.get(rabbitId)
        ?: throw RuntimeException(QueueItemNotFoundException)
    queueItem.status = "waiting"
    redisAdapter.set(rabbitId, queueItem)
  }

  @MessageExceptionHandler
  suspend fun handleException(e: Exception) {
    throw e
  }

}
