package org.moeawebframework.processor

import org.junit.jupiter.api.*
import org.moeaframework.core.spi.ProviderNotFoundException
import org.moeawebframework.processor.configs.MinioTestConfig
import org.moeawebframework.processor.configs.RabbitMQTestConfig
import org.moeawebframework.processor.configurations.default_algorithms
import org.moeawebframework.processor.configurations.default_problems
import org.moeawebframework.processor.dto.QueueItemDTO
import org.moeawebframework.processor.entities.QueueItem
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest
@TestMethodOrder(MethodOrderer.Alphanumeric::class)
@Import(value = [RabbitMQTestConfig::class, MinioTestConfig::class])
class ProcessorTests {

  @Autowired
  @Qualifier("mockMinioAdapter")
  lateinit var minioAdapter: MinioAdapter

  @Autowired
  lateinit var processor: Processor

  // 1. test for non user

  // 1.1 test with a standard algorithm/problem
  // 1.2 test with a wrong algorithm/problem
  // 1.3 test with a standard algorithm and a wrong problem
  // 1.4 test with a standard problem and a wrong algorithm

  // 2. test for user

  // 2.1 test with a standard algorithm/problem
  // 2.2 test with a wrong algorithm/problem
  // 2.3 test with a standard algorithm and a wrong problem
  // 2.4 test with a standard problem and a wrong algorithm
  // 2.5 test with a custom algorithm/problem
  // 2.6 test with a custom algorithm and a default problem
  // 2.7 test with a custom problem and a default algorithm
  // 2.8 test with a custom algorithm and a wrong problem
  // 2.9 test with a custom problem and a wrong algorithm


  val queueItemDTO = QueueItemDTO(numberOfSeeds = 1, numberOfEvaluations = 500)

  // 1. test for non user

  @Test
  fun `1_1 test with a standard algorithm and problem`() {
    queueItemDTO.name = "1_1"
    queueItemDTO.algorithmMD5 = default_algorithms[0]
    queueItemDTO.problemMD5 = default_problems[0]
    queueItemDTO.referenceSetMD5 = default_problems[0]
    assertDoesNotThrow {
      processor.startProcessing(QueueItem(queueItemDTO, "uuid_1_1", null))
    }
  }

  @Test
  fun `1_2 test with a wrong algorithm and problem`() {
    queueItemDTO.name = "1_2"
    queueItemDTO.algorithmMD5 = "wrong_algorithm"
    queueItemDTO.problemMD5 = "wrong_problem"
    queueItemDTO.referenceSetMD5 = "wrong_problem"
    assertThrows<ProviderNotFoundException>("no provider for wrong_problem") {
      processor.startProcessing(QueueItem(queueItemDTO, "uuid_1_2", null))
    }
  }

  @Test
  fun `1_3 test with a standard algorithm and a wrong problem`() {
    queueItemDTO.name = "1_3"
    queueItemDTO.algorithmMD5 = default_algorithms[0]
    queueItemDTO.problemMD5 = "wrong_problem"
    queueItemDTO.referenceSetMD5 = "wrong_problem"
    assertThrows<ProviderNotFoundException>("no provider for wrong_problem") {
      processor.startProcessing(QueueItem(queueItemDTO, "uuid_1_3", null))
    }
  }

  @Test
  fun `1_4 test with a standard problem and a wrong algorithm`() {
    queueItemDTO.name = "1_4"
    queueItemDTO.algorithmMD5 = "wrong_algorithm"
    queueItemDTO.problemMD5 = default_problems[0]
    queueItemDTO.referenceSetMD5 = "wrong_problem"
    assertThrows<ProviderNotFoundException>("no provider for wrong_algorithm") {
      processor.startProcessing(QueueItem(queueItemDTO, "uuid_1_4", null))
    }
  }

  // 2. test for user

  @Test
  fun `2_1 test with a standard algorithm and problem`() {
    queueItemDTO.name = "2_1"
    queueItemDTO.algorithmMD5 = default_algorithms[0]
    queueItemDTO.problemMD5 = default_problems[0]
    queueItemDTO.referenceSetMD5 = default_problems[0]
    assertDoesNotThrow {
      processor.startProcessing(QueueItem(queueItemDTO, "uuid_2_1", "cdd36e48-f1c5-474e-abc3-ac7a17909878"))
    }
  }

  @Test
  fun `2_2 test with a wrong algorithm and problem`() {
    queueItemDTO.name = "2_2"
    queueItemDTO.algorithmMD5 = "wrong_algorithm"
    queueItemDTO.problemMD5 = "wrong_problem"
    queueItemDTO.referenceSetMD5 = "wrong_problem"
    assertThrows<ProviderNotFoundException> {
      processor.startProcessing(QueueItem(queueItemDTO, "uuid_2_2", "cdd36e48-f1c5-474e-abc3-ac7a17909878"))
    }
  }

  @Test
  fun `2_3 test with a standard algorithm and a wrong problem`() {

  }

  @Test
  fun `2_4 test with a standard problem and a wrong algorithm`() {

  }

  @Test
  fun `2_5 test with a custom algorithm and problem`() {

  }

  @Test
  fun `2_6 test with a custom algorithm and a default problem`() {

  }

  @Test
  fun `2_7 test with a custom problem and a default algorithm`() {

  }

  @Test
  fun `2_8 test with a custom algorithm and a wrong problem`() {

  }

  @Test
  fun `2_9 test with a custom problem and a wrong algorithm`() {

  }

}