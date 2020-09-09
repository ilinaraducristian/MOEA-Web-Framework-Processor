package org.moeawebframework.processor.moea

import org.moeaframework.Instrumenter
import org.moeaframework.analysis.collector.InstrumentedAlgorithm
import org.moeaframework.analysis.sensitivity.EpsilonHelper
import org.moeaframework.core.Algorithm

class Instrumenter : Instrumenter() {

  override fun instrument(algorithm: Algorithm?): InstrumentedAlgorithm {
    try {
      withEpsilon(EpsilonHelper.getEpsilon(algorithm?.problem))
    } catch (ignored: Exception) {
    }
    return super.instrument(algorithm)
  }

}