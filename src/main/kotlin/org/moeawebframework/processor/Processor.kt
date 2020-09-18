package org.moeawebframework.processor

import org.moeaframework.Executor
import org.moeaframework.Instrumenter
import org.moeaframework.util.progress.ProgressEvent
import org.moeawebframework.processor.entities.Process
import org.springframework.amqp.rabbit.core.RabbitTemplate
import reactor.core.publisher.Mono
import java.util.*

val processors = HashMap<String, Processor>()

class Processor(
    private val rabbitTemplate: RabbitTemplate,
    val process: Process
) {

  private val instrumenter = Instrumenter().attachAll()
  private val executor: Executor
  private val results = Results()

  init {
    executor = Executor()
        .withInstrumenter(instrumenter)
        .withMaxEvaluations(process.numberOfEvaluations)
        .withProgressListener { event: ProgressEvent ->
          if (event.isSeedFinished) return@withProgressListener

          sendResult(process.rabbitId, results.update(event).toByteArray())

        }
    val referenceSetSha256 = process.referenceSetSha256
//    // TODO
//    // doing this works but it calls the CDN twice, once for the instrumenter and once for the executor
//    // one possible approach would be to load all the algorithms and problems in one place and then add them to both instrumenter and executor
//    // another approach would be to re implement the problem builder, instrumenter, executor and the other classes that are using the ProblemBuilder class
//    // the third approach would be to fork the moea repo and change the ProblemBuilder visibility and also fix other problems of this library
    if (referenceSetSha256.isEmpty()) {
      instrumenter.withProblem(process.problemSha256)
      executor.withProblem(process.problemSha256)
    } else {
      instrumenter.withProblem(process.problemSha256 + "#" + process.referenceSetSha256)
      executor.withProblem(process.problemSha256 + "#" + process.referenceSetSha256)
    }
    executor.withAlgorithm(process.algorithmSha256)
  }

  fun startProcessing(): Boolean {
    processors[process.rabbitId] = this
    executor.runSeeds(process.numberOfSeeds)

    return !executor.isCanceled
  }

  fun cancel() {
    executor.cancel()
    processors.remove(process.rabbitId)
  }

  fun getResults(): String {
    return results.toJson()
  }

  private fun sendResult(queue: String, result: ByteArray): Mono<Unit> {
    rabbitTemplate.convertAndSend(queue, result)
    return Mono.empty()
  }

}