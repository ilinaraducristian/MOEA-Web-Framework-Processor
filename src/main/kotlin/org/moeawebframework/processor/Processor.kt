package org.moeawebframework.processor

import io.minio.GetObjectArgs
import io.minio.MinioClient
import org.moeaframework.Executor
import org.moeaframework.Instrumenter
import org.moeaframework.algorithm.StandardAlgorithms
import org.moeaframework.core.Algorithm
import org.moeaframework.core.NondominatedPopulation
import org.moeaframework.core.PopulationIO
import org.moeaframework.core.Problem
import org.moeaframework.core.spi.AlgorithmFactory
import org.moeaframework.core.spi.ProblemFactory
import org.moeaframework.problem.StandardProblems
import org.moeawebframework.processor.configurations.default_algorithms
import org.moeawebframework.processor.configurations.default_problems
import org.moeawebframework.processor.entities.QueueItem
import org.moeawebframework.processor.moea.BytesClassLoader
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.StringReader
import java.nio.charset.Charset
import java.util.*

val processors = HashMap<String, Executor>()

@Component
class Processor(
    private val rabbitTemplate: RabbitTemplate,
    private val minioClient: MinioClient
) {

  @Value("\${minio.bucket}")
  lateinit var bucket: String

  private val cdnAlgorithmFactory = CDNAlgorithmFactory()
  private val cdnProblemFactory = CDNProblemFactory()
  private val standardAlgorithms = StandardAlgorithms()
  private val standardProblems = StandardProblems()

  private val results = Results()

  fun startProcessing(queueItem: QueueItem): Results {
    val instrumenter = Instrumenter().attachAll()
    val executor = Executor().withInstrumenter(instrumenter)
        .withMaxEvaluations(queueItem.numberOfEvaluations)
        .withProgressListener { event ->
          if (event.isSeedFinished) return@withProgressListener
          rabbitTemplate.convertAndSend(queueItem.rabbitId, results.update(event))
        }
    executor.withAlgorithm(queueItem.algorithmMD5)

    if (queueItem.userEntityId == null) {
      executor.withProblem(queueItem.problemMD5)
    } else {
      if (!default_problems.contains(queueItem.referenceSetMD5)) {
        executor.withProblem("${queueItem.problemMD5}#${queueItem.referenceSetMD5}")
            .usingProblemFactory(cdnProblemFactory)
      } else {
        executor.withProblem(queueItem.problemMD5)
      }
      if (!default_algorithms.contains(queueItem.algorithmMD5)) {
        executor.usingAlgorithmFactory(cdnAlgorithmFactory)
      }
    }

    processors[queueItem.rabbitId] = executor
    executor.runSeeds(queueItem.numberOfSeeds)
    return results
  }

  inner class CDNAlgorithmFactory : AlgorithmFactory() {

    override fun getAlgorithm(name: String, properties: Properties, problem: Problem): Algorithm? {
      try {
        val objectArgs = GetObjectArgs.builder()
            .bucket(bucket)
            .`object`(name)
            .build()
        val algorithmClass = BytesClassLoader<Algorithm>(ProcessorApplication::class.java.classLoader).loadClassFromBytes(minioClient.getObject(objectArgs).readAllBytes())
        return algorithmClass.getConstructor(Properties::class.java, Problem::class.java).newInstance(properties, problem)
      } catch (e: Exception) {
      }
      return try {
        standardAlgorithms.getAlgorithm(name, properties, problem)
      } catch (e: Exception) {
        null
      }
    }

  }

  inner class CDNProblemFactory : ProblemFactory() {

    override fun getProblem(name: String): Problem? {
      try {
        if (name.contains("#")) {
          val problemName = name.split("#")[0]
          val objectArgs = GetObjectArgs.builder()
              .bucket(bucket)
              .`object`(problemName)
              .build()
          return BytesClassLoader<Problem>(ProcessorApplication::class.java.classLoader)
              .loadClassFromBytes(minioClient.getObject(objectArgs).readAllBytes())
              .getDeclaredConstructor().newInstance()
        }
      } catch (e: Exception) {
      }
      return standardProblems.getProblem(name)
    }

    override fun getReferenceSet(name: String): NondominatedPopulation? {
      try {
        if (name.contains("#")) {
          val referenceSetName = name.split("#")[1]
          val objectArgs = GetObjectArgs.builder()
              .bucket(bucket)
              .`object`(referenceSetName)
              .build()
          val referenceSet = minioClient.getObject(objectArgs).readAllBytes().toString(Charset.forName("utf-8"))
          return NondominatedPopulation(PopulationIO.readObjectives(BufferedReader(StringReader(referenceSet))))
        }
      } catch (e: Exception) {
      }
      return standardProblems.getReferenceSet(name)
    }

  }

}