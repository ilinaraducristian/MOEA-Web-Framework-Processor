package org.moeawebframework.processor

import org.moeaframework.Executor
import org.moeaframework.Instrumenter
import org.moeaframework.util.progress.ProgressEvent
import org.moeawebframework.processor.entities.Process
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import reactor.core.publisher.Mono
import java.util.*

val processors = HashMap<String, Processor>()

class Processor(val newProcess: Process) {

  @Autowired
  private lateinit var rabbitTemplate: RabbitTemplate

  private val instrumenter = Instrumenter()
      .withFrequency(1)
      .attachHypervolumeCollector()
      .attachGenerationalDistanceCollector()
      .attachInvertedGenerationalDistanceCollector()
      .attachSpacingCollector()
      .attachAdditiveEpsilonIndicatorCollector()
      .attachContributionCollector()
      .attachR1Collector()
      .attachR2Collector()
      .attachR3Collector()
      .attachEpsilonProgressCollector()
      .attachAdaptiveMultimethodVariationCollector()
      .attachAdaptiveTimeContinuationCollector()
      .attachElapsedTimeCollector()
      .attachApproximationSetCollector()
      .attachPopulationSizeCollector()
  private val executor: Executor
  private val results = Results()

  init {
    executor = Executor()
        .withInstrumenter(instrumenter)
        .withMaxEvaluations(newProcess.numberOfEvaluations)
        .withProgressListener { progressEvent: ProgressEvent ->
          if (progressEvent.isSeedFinished) return@withProgressListener
          sendResult(newProcess.rabbitId, results.update(progressEvent).toByteArray())
        }
    val referenceSetSha256 = newProcess.referenceSetSha256

    if (referenceSetSha256.isEmpty()) instrumenter.withProblem(newProcess.problemSha256) else instrumenter.withProblem(newProcess.problemSha256 + "#" + newProcess.referenceSetSha256)
    executor.withAlgorithm(newProcess.algorithmSha256)
    // TODO
    // doing this works but it calls the CDN twice, once for the instrumenter and once for the executor
    // one possible approach would be to load all the algorithms and problems in one place and then add them to both instrumenter and executor
    // another approach would be to re implement the problem builder, instrumenter, executor and the other classes that are using the ProblemBuilder class
    // the third approach would be to fork the moea repo and change the ProblemBuilder visibility and also fix other problems of this library
    if (referenceSetSha256.isEmpty()) executor.withProblem(newProcess.problemSha256) else executor.withProblem(newProcess.problemSha256 + "#" + newProcess.referenceSetSha256)
  }

  fun process(): Boolean {
    processors[newProcess.rabbitId] = this
    println("oink 1")
    executor.runSeeds(newProcess.numberOfSeeds)
    println("oink 2")
    return !executor.isCanceled
  }

  fun cancel() {
    executor.cancel()
    processors.remove(newProcess.rabbitId)
  }

  fun getResults(): String {
    return results.toJson()
  }

  private fun sendResult(queue: String, result: ByteArray): Mono<Unit> {
    println("Result")
    println(result.size)
    rabbitTemplate.convertAndSend(queue, result)
    return Mono.empty()
  }

}