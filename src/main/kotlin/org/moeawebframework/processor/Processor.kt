package org.moeawebframework.processor

import org.moeaframework.Executor
import org.moeaframework.Instrumenter
import org.moeaframework.algorithm.StandardAlgorithms
import org.moeaframework.core.Algorithm
import org.moeaframework.core.NondominatedPopulation
import org.moeaframework.core.PopulationIO
import org.moeaframework.core.Problem
import org.moeaframework.core.spi.AlgorithmFactory
import org.moeaframework.core.spi.ProblemFactory
import org.moeaframework.core.spi.ProviderNotFoundException
import org.moeaframework.problem.StandardProblems
import org.moeawebframework.processor.adapters.MinioAdapter
import org.moeawebframework.processor.configurations.default_algorithms
import org.moeawebframework.processor.configurations.default_problems
import org.moeawebframework.processor.entities.QueueItem
import org.moeawebframework.processor.moea.BytesClassLoader
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.StringReader
import java.nio.charset.Charset
import java.util.*

val processors = HashMap<String, Executor>()

@Component
class Processor(
    private val rabbitTemplate: RabbitTemplate,
    private val minioAdapter: MinioAdapter
) {

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
      instrumenter.withProblem(queueItem.problemMD5)
    } else {
      if (!default_problems.contains(queueItem.referenceSetMD5)) {
        executor.withProblem("${queueItem.problemMD5}#${queueItem.referenceSetMD5}")
            .usingProblemFactory(cdnProblemFactory)
        instrumenter.withProblem("${queueItem.problemMD5}#${queueItem.referenceSetMD5}")
            .usingProblemFactory(cdnProblemFactory)
      } else {
        executor.withProblem(queueItem.problemMD5)
        instrumenter.withProblem(queueItem.problemMD5)
      }
      if (!default_algorithms.contains(queueItem.algorithmMD5)) {
        executor.usingAlgorithmFactory(cdnAlgorithmFactory)
      }
    }

    processors[queueItem.rabbitId] = executor
    executor.runSeeds(queueItem.numberOfSeeds)
    return results
  }

  private inner class CDNAlgorithmFactory : AlgorithmFactory() {

    override fun getAlgorithm(name: String, properties: Properties, problem: Problem?): Algorithm? {
      var algorithm: Algorithm? = null
      try {
        if (problem == null) throw RuntimeException("problem is null")
        val classBytes = minioAdapter.download(name) ?: return null
        val algorithmClass = BytesClassLoader<Algorithm>(ProcessorApplication::class.java.classLoader).loadClassFromBytes(classBytes)
        algorithm = algorithmClass.getConstructor(Properties::class.java, Problem::class.java).newInstance(properties, problem)
      } catch (e: Exception) {
      }
      if (algorithm != null) return algorithm
      return standardAlgorithms.getAlgorithm(name, properties, problem)
          ?: throw ProviderNotFoundException(name, RuntimeException())
    }

  }

  private inner class CDNProblemFactory : ProblemFactory() {

    override fun getProblem(name: String): Problem? {
      var problem: Problem? = null
      val problemName = name.split("#")[0]
      try {
        if (name.contains("#")) {
          val problemBytes = minioAdapter.download(problemName) ?: return null
          problem = BytesClassLoader<Problem>(ProcessorApplication::class.java.classLoader)
              .loadClassFromBytes(problemBytes)
              .getDeclaredConstructor().newInstance()
        }
      } catch (e: Exception) {
      }
      if (problem != null) return problem
      return standardProblems.getProblem(name) ?: throw ProviderNotFoundException(problemName, RuntimeException())
    }

    override fun getReferenceSet(name: String): NondominatedPopulation? {
      var nondominatedPopulation: NondominatedPopulation? = null
      val referenceSetName = name.split("#")[1]
      try {
        if (name.contains("#")) {
          val referecenSetBytes = minioAdapter.download(referenceSetName) ?: return null
          val referenceSet = referecenSetBytes.toString(Charset.defaultCharset())
          nondominatedPopulation = NondominatedPopulation(PopulationIO.readObjectives(BufferedReader(StringReader(referenceSet))))
        }
      } catch (e: Exception) {
      }
      if (nondominatedPopulation != null) return nondominatedPopulation
      return standardProblems.getReferenceSet(name)
          ?: throw ProviderNotFoundException(referenceSetName, RuntimeException())
    }

  }

}