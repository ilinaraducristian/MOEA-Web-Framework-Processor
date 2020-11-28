package org.moeawebframework.processor.configs

import org.mockito.Mockito
import org.moeawebframework.processor.adapters.MinioAdapter
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class MinioTestConfig {

  @Bean
  fun mockMinioAdapter(): MinioAdapter {
    val minioAdapter = Mockito.mock(MinioAdapter::class.java)
    Mockito.`when`(minioAdapter.download(Mockito.anyString())).then {
      val name = it.getArgument<String>(0)
      if (name == "wrong_problem" || name == "wrong_algorithm") {
        return@then null
      }
      return@then ""
    }
    return minioAdapter
  }

}