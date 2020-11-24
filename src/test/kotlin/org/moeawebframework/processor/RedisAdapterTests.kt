package org.moeawebframework.processor

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.moeawebframework.processor.dto.QueueItemDTO
import org.moeawebframework.processor.entities.QueueItem
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class RedisAdapterTests {

  @Autowired
  lateinit var redisAdapter: RedisAdapter

  @Test
  fun `should store, receive and delete a queue item in redis`() = runBlocking {
    val uuid = "f9ace504-969c-4e88-a234-1177c3ad81e9"
    val queueItemDTO = QueueItemDTO(
        "Queue item name",
        15000,
        10,
        "CMA-ES",
        "Belegundu",
        ""
    )
    val queueItem = QueueItem(queueItemDTO, uuid, null)
    assert(redisAdapter.set(uuid, queueItem))
    Assertions.assertEquals(uuid, redisAdapter.get(uuid)?.rabbitId)
  }

}