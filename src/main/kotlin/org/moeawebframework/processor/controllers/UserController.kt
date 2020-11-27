package org.moeawebframework.processor.controllers

import kotlinx.coroutines.runBlocking
import org.moeawebframework.processor.Processor
import org.moeawebframework.processor.QueueItemNotFoundException
import org.moeawebframework.processor.dao.QueueItemDAO
import org.moeawebframework.processor.entities.QueueItem
import org.moeawebframework.processor.processors
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.util.concurrent.ExecutorService

@Controller
@MessageMapping("user")
class UserController(
    private val executorService: ExecutorService,
    private val queueItemDAO: QueueItemDAO,
    private val processor: Processor
) {

  @MessageMapping("startProcessing")
  suspend fun process(queueItem: QueueItem) {
    queueItemDAO.save(queueItem)
    executorService.submit {
      try {
        queueItem.results = processor.startProcessing(queueItem).toJson()
        if (processors[queueItem.rabbitId]?.isCanceled!!) {
          queueItem.status = "waiting"
        } else {
          queueItem.status = "processed"
        }
        runBlocking {
          queueItemDAO.save(queueItem)
        }
      } catch (e: Exception) {
        queueItem.status = "waiting"
        runBlocking {
          queueItemDAO.save(queueItem)
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
