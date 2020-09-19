package org.moeawebframework.processor.moea

import org.moeaframework.core.NondominatedPopulation
import org.moeaframework.core.PopulationIO
import org.moeaframework.core.Problem
import org.moeaframework.core.spi.ProblemProvider
import org.moeawebframework.processor.ProcessorApplication
import org.moeawebframework.processor.configurations.MainConfig
import org.springframework.http.HttpStatus
import java.io.BufferedReader
import java.io.StringReader

class CDNProblemProvider : ProblemProvider() {

  override fun getProblem(sha256: String): Problem? {
    try {
      if (!sha256.contains("#")) return null
      val problemSha256 = sha256.split("#")[0]
      val clientResponse = MainConfig.getFromCDN(problemSha256).block()!!
      val problemBytes = clientResponse.bodyToMono(ByteArray::class.java).block()!!
      if (clientResponse.statusCode() == HttpStatus.NOT_FOUND) return null
      val problemClass = BytesClassLoader<Problem>(ProcessorApplication::class.java.classLoader).loadClassFromBytes(problemBytes)
      return problemClass.getDeclaredConstructor().newInstance()
    } catch (e: Exception) {
      return null
    }
  }

  override fun getReferenceSet(sha256: String): NondominatedPopulation? {
    try {
      if (!sha256.contains("#")) return null
      val referenceSetSha256 = sha256.split("#")[1]
      val clientResponse = MainConfig.getFromCDN(referenceSetSha256).block()!!
      val referenceSetString = clientResponse.bodyToMono(String::class.java).block()!!
      if (clientResponse.statusCode() == HttpStatus.NOT_FOUND) return null
      return NondominatedPopulation(PopulationIO.readObjectives(BufferedReader(StringReader(referenceSetString))))
    } catch (e: Exception) {
      e.printStackTrace()
      return null
    }
  }

}