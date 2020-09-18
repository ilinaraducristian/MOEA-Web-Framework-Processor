package org.moeawebframework.processor.moea

import org.moeaframework.core.Algorithm
import org.moeaframework.core.Problem
import org.moeaframework.core.spi.AlgorithmProvider
import org.moeawebframework.processor.ProcessorApplication
import org.moeawebframework.processor.configurations.getFromCDN
import org.springframework.http.HttpStatus
import java.util.*

class CDNAlgorithmProvider : AlgorithmProvider() {

  override fun getAlgorithm(sha256: String, properties: Properties, problem: Problem): Algorithm? {
    try {
      val clientResponse = getFromCDN(sha256).block()
      val algorithmBytes = clientResponse!!.bodyToMono(ByteArray::class.java).block()!!
      if (clientResponse.statusCode() == HttpStatus.NOT_FOUND) return null
      val algorithmClass = BytesClassLoader<Algorithm>(ProcessorApplication::class.java.classLoader).loadClassFromBytes(algorithmBytes)
      return algorithmClass.getConstructor(Properties::class.java, Problem::class.java).newInstance(properties, problem)
    } catch (ignored: Exception) {
      return null
    }
  }

}