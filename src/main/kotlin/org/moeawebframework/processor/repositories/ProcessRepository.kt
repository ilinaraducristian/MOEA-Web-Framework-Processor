package org.moeawebframework.processor.repositories

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Mono
import org.moeawebframework.processor.entities.Process

interface ProcessRepository : R2dbcRepository<Process, Long> {

  //  @Query("SELECT * FROM problem_solvers WHERE user_id = :userId")
  fun findByUserId(userId: Long): Mono<Process>

  fun findByRabbitId(rabbitId: String): Mono<Process>

  @Query("UPDATE processes SET status = :status WHERE id = :id")
  fun updateStatus(id: Long, status: String)

}