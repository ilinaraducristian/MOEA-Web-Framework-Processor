package org.moeawebframework.processor

import org.moeaframework.core.spi.AlgorithmFactory
import org.moeaframework.core.spi.ProblemFactory
import org.moeawebframework.processor.moea.CDNAlgorithmProvider
import org.moeawebframework.processor.moea.CDNProblemProvider
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ProcessorApplication

fun main(args: Array<String>) {
  AlgorithmFactory.getInstance().addProvider(CDNAlgorithmProvider())
  ProblemFactory.getInstance().addProvider(CDNProblemProvider())
  runApplication<ProcessorApplication>(*args) {
    webApplicationType = WebApplicationType.NONE
  }
//  val executors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
//  val process = Process()
//  process.rabbitId = "asd"
//  process.algorithmSha256 = "CMA-ES"
//  process.name = "aASD"
//  process.numberOfEvaluations = 10000
//  process.numberOfSeeds = 10
//  process.problemSha256 = "Belegundu"
//  val processor = Processor(process)
//  executors.submit{
//
//  try {
//    processor.process()
//    process.results = processor.getResults()
//    process.status = "processed"
//    println(process.results)
////    if (process.userId == null) {
////      redisTemplate.opsForValue().set(newProcess.rabbitId, newProcess).block()
////    } else {
////      processDAO.save(it.newProcess).block()
////    }
//  } catch (e: Exception) {
////    it.newProcess.status = "waiting"
//    println("Exception in processor, it should never happen")
//    e.printStackTrace()
//  }
//  }
}
