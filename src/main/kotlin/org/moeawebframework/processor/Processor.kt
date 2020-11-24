package org.moeawebframework.processor

import org.moeaframework.Executor
import org.moeaframework.Instrumenter
import org.moeaframework.util.progress.ProgressEvent
import org.moeawebframework.processor.entities.QueueItem
import org.springframework.amqp.rabbit.core.RabbitTemplate
import reactor.core.publisher.Mono
import java.util.*

val processors = HashMap<String, Processor>()

class Processor(
    private val rabbitTemplate: RabbitTemplate,
    val queueItem: QueueItem
) {

  private val instrumenter = Instrumenter().attachAll()
  private val executor: Executor
  private val results = Results()

  init {
    executor = Executor()
        .withInstrumenter(instrumenter)
        .withMaxEvaluations(queueItem.numberOfEvaluations)
        .withProgressListener { event: ProgressEvent ->
          if (event.isSeedFinished) return@withProgressListener
          sendResult(queueItem.rabbitId, results.update(event).toByteArray())
        }
    val referenceSetMD5 = queueItem.referenceSetMD5
    if (queueItem.userEntityId == null || referenceSetMD5.isBlank()) {
      instrumenter.withProblem(queueItem.problemMD5)
      executor.withProblem(queueItem.problemMD5)
    } else {
      instrumenter.withProblem(queueItem.problemMD5 + "#" + queueItem.referenceSetMD5)
      executor.withProblem(queueItem.problemMD5 + "#" + queueItem.referenceSetMD5)
    }
    executor.withAlgorithm(queueItem.algorithmMD5)
  }

  fun startProcessing(): Boolean {
    processors[queueItem.rabbitId] = this
    executor.runSeeds(queueItem.numberOfSeeds)

    return !executor.isCanceled
  }

  fun cancel() {
    executor.cancel()
    processors.remove(queueItem.rabbitId)
  }

  fun getResults(): String {
    return results.toJson()
  }

  private fun sendResult(queue: String, result: ByteArray): Mono<Unit> {
    rabbitTemplate.convertAndSend(queue, result)
    return Mono.empty()
  }

}