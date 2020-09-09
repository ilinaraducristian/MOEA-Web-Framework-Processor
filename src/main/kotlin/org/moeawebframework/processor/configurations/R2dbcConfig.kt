package org.moeawebframework.processor.configurations

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ClassPathResource
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.connectionfactory.init.CompositeDatabasePopulator
import org.springframework.data.r2dbc.connectionfactory.init.ConnectionFactoryInitializer
import org.springframework.data.r2dbc.connectionfactory.init.ResourceDatabasePopulator


@Configuration
class R2dbcConfig : AbstractR2dbcConfiguration() {

  @Value("\${r2dbc_uri}")
  lateinit var r2dbc_uri: String

  override fun connectionFactory(): ConnectionFactory {
    return ConnectionFactories.get(r2dbc_uri)
  }

  @Bean
  @Profile("dev")
  fun initializer(): ConnectionFactoryInitializer {
    val initializer = ConnectionFactoryInitializer()
    initializer.setConnectionFactory(connectionFactory())
    val populator = CompositeDatabasePopulator()
    populator.addPopulators(ResourceDatabasePopulator(ClassPathResource("schema.sql")))
    populator.addPopulators(ResourceDatabasePopulator(ClassPathResource("data.sql")))
    initializer.setDatabasePopulator(populator)
    return initializer
  }

}