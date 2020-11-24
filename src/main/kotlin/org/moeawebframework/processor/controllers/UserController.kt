package org.moeawebframework.processor.controllers

import kotlinx.coroutines.runBlocking
import org.moeawebframework.processor.Processor
import org.moeawebframework.processor.QueueItemNotFoundException
import org.moeawebframework.processor.dao.QueueItemDAO
import org.moeawebframework.processor.entities.QueueItem
import org.moeawebframework.processor.processors
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.util.concurrent.ExecutorService

@Controller
@MessageMapping("user")
class UserController(
    private val rabbitTemplate: RabbitTemplate,
    private val executorService: ExecutorService,
    private val queueItemDAO: QueueItemDAO
) {

  @MessageMapping("startProcessing")
  suspend fun process(queueItem: QueueItem) {
    val processor = Processor(rabbitTemplate, queueItem)
    queueItem.status = "working"
    queueItemDAO.save(queueItem)
    executorService.submit {
      try {
        processor.startProcessing()
        val newProcess = processor.queueItem
        newProcess.results = processor.getResults()
        newProcess.status = "processed"
        runBlocking {
          queueItemDAO.save(processor.queueItem)
        }
      } catch (e: Exception) {
        processor.queueItem.status = "waiting"
        runBlocking {
          queueItemDAO.save(processor.queueItem)
        }
        println("Exception in processor, it should never happen")
        e.printStackTrace()
      }
    }
  }

  @MessageMapping("cancelProcessing")
  suspend fun cancel(rabbitId: String) {
    if (processors[rabbitId] == null) throw RuntimeException(QueueItemNotFoundException)
    processors[rabbitId]?.cancel()
    val queueItem = queueItemDAO.getByRabbitId(rabbitId) ?: throw RuntimeException(QueueItemNotFoundException)
    queueItem.status = "waiting"
    queueItemDAO.save(queueItem)
  }

  @MessageExceptionHandler
  suspend fun handleException(e: Exception) {
    throw e
  }

}
