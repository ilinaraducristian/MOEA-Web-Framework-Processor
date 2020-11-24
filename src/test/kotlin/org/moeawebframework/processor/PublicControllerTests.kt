package org.moeawebframework.processor

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.moeaframework.core.spi.AlgorithmFactory
import org.moeaframework.core.spi.ProblemFactory
import org.moeawebframework.processor.configs.R2dbcTestConfig
import org.moeawebframework.processor.configs.RabbitMQTestConfig
import org.moeawebframework.processor.controllers.PublicController
import org.moeawebframework.processor.dto.QueueItemDTO
import org.moeawebframework.processor.entities.QueueItem
import org.moeawebframework.processor.moea.CDNAlgorithmProvider
import org.moeawebframework.processor.moea.CDNProblemProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest
@Import(value = [R2dbcTestConfig::class, RabbitMQTestConfig::class])
class PublicControllerTests {

  companion object {

    @BeforeAll
    @JvmStatic
    fun beforeAll() {
      AlgorithmFactory.getInstance().addProvider(CDNAlgorithmProvider())
      ProblemFactory.getInstance().addProvider(CDNProblemProvider())
    }

  }

  @Autowired
  lateinit var publicController: PublicController

  @Test
  fun `should start processing`() = runBlocking {
    val uuid = "8e8529eb-5074-4302-bfd1-8d796567341c"
    val queueItemDTO = QueueItemDTO(
        "Queue item name",
        15000,
        10,
        "CMA-ES",
        "Belegundu",
        ""
    )
    publicController.startProcessing(QueueItem(queueItemDTO, uuid, null))
  }

}