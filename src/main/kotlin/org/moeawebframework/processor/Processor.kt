package org.moeawebframework.processor

import org.moeaframework.Executor
import org.moeaframework.Instrumenter
import org.moeaframework.util.progress.ProgressEvent
import org.moeawebframework.processor.dto.ProcessDTO
import org.moeawebframework.processor.entities.Process
import reactor.core.publisher.Mono
import java.util.*

class Processor(val newProcess: Process) {
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

  fun process(): Boolean {
    executor.runSeeds(newProcess.numberOfSeeds)
    return !executor.isCanceled
  }

  fun cancel() {
    executor.cancel()
  }

  private fun sendResult(queue: String, result: ByteArray): Mono<Void> {
    return Mono.empty()
    //    return Main.sender.send(Mono.just(new OutboundMessage("", queue, result)));
  }

  fun getResults(): String {
    return results.toJson()
  }

  init {
    executor = Executor()
        .withInstrumenter(instrumenter)
        .withMaxEvaluations(newProcess.numberOfEvaluations)
        .withProgressListener { progressEvent: ProgressEvent ->
          if (progressEvent.isSeedFinished) return@withProgressListener
          results.update(progressEvent)
          sendResult(newProcess.rabbitId, results.toString().toByteArray()) // set rabbitmq queue limit to only one message (that contains all results up to that point)
        }
    val referenceSetSha256 = newProcess.referenceSetSha256
    if (referenceSetSha256.isEmpty()) instrumenter.withProblem(newProcess.problemSha256) else instrumenter.withProblem(newProcess.problemSha256 + "#" + newProcess.referenceSetSha256)

//    try {
//      instrumenter.withEpsilon(EpsilonHelper.getEpsilon(problem));
//    } catch (Exception ignored) {
//    }
    executor.withAlgorithm(newProcess.algorithmSha256)
    executor.withSameProblemAs(instrumenter)
  }
}