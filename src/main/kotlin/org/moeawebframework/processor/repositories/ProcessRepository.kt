package org.moeawebframework.processor.repositories

import org.moeawebframework.processor.entities.QueueItem
import org.springframework.data.r2dbc.repository.R2dbcRepository

interface QueueItemRepository : R2dbcRepository<QueueItem, Long> {

  suspend fun findByRabbitId(rabbitId: String): QueueItem?

}