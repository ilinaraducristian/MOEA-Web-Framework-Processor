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
//    try {
//      instrumenter.withEpsilon(EpsilonHelper.getEpsilon(problem))
//    } catch (ignored: Exception) {
//    }
    executor.withAlgorithm(newProcess.algorithmSha256)
    executor.withSameProblemAs(instrumenter)
  }

  fun process(): Boolean {
    processors[newProcess.rabbitId] = this
    executor.runSeeds(newProcess.numberOfSeeds)
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
    rabbitTemplate.convertAndSend(queue, result)
    return Mono.empty()
  }

}