package org.moeawebframework.processor.dao

import org.moeawebframework.processor.entities.QueueItem
import org.moeawebframework.processor.repositories.QueueItemRepository
import org.springframework.stereotype.Repository

@Repository
class QueueItemDAO(
    private val queueItemRepository: QueueItemRepository
) : DAO<QueueItem, Long>(queueItemRepository) {

  suspend fun getByRabbitId(rabbitId: String): QueueItem? {
    return queueItemRepository.findByRabbitId(rabbitId)
  }

}