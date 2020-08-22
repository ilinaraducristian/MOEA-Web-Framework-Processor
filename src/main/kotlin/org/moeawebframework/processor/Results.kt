package org.moeawebframework.processor

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.moeaframework.util.progress.ProgressEvent
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

internal class Results {

  @Autowired
  lateinit var objectMapper: ObjectMapper

  private var currentSeed = 0
  private val results = HashMap<String, ArrayList<ArrayList<Double>>>()

  init {
    results["R1Indicator"] = ArrayList()
    results["AdditiveEpsilonIndicator"] = ArrayList()
    results["R2Indicator"] = ArrayList()
    results["GenerationalDistance"] = ArrayList()
    results["Hypervolume"] = ArrayList()
    results["Spacing"] = ArrayList()
    results["R3Indicator"] = ArrayList()
    results["InvertedGenerationalDistance"] = ArrayList()
    results["ElapsedTime"] = ArrayList()
    results["Contribution"] = ArrayList()
  }

  fun update(progressEvent: ProgressEvent): String {
    val accumulator = progressEvent.executor.instrumenter.lastAccumulator
    val i = accumulator.size("NFE") - 1
    currentSeed = progressEvent.currentSeed - 1
    if (i == 0) {
      results.forEach { (_: String, arrayLists: ArrayList<ArrayList<Double>>) -> arrayLists.add(ArrayList()) }
    }
    results.forEach { (s: String, arrayLists: ArrayList<ArrayList<Double>>) -> if (s == "ElapsedTime") arrayLists[currentSeed].add(accumulator["Elapsed Time", i] as Double) else arrayLists[currentSeed].add(accumulator[s, i] as Double) }
    return currentSeedToJson()
  }

  private fun currentSeedToJson(): String {
    val json: ObjectNode = objectMapper.createObjectNode()
    json.putPOJO("R1Indicator", results["R1Indicator"]!![currentSeed])
    json.putPOJO("AdditiveEpsilonIndicator", results["AdditiveEpsilonIndicator"]!![currentSeed])
    json.putPOJO("R2Indicator", results["R2Indicator"]!![currentSeed])
    json.putPOJO("GenerationalDistance", results["GenerationalDistance"]!![currentSeed])
    json.putPOJO("Hypervolume", results["Hypervolume"]!![currentSeed])
    json.putPOJO("Spacing", results["Spacing"]!![currentSeed])
    json.putPOJO("R3Indicator", results["R3Indicator"]!![currentSeed])
    json.putPOJO("InvertedGenerationalDistance", results["InvertedGenerationalDistance"]!![currentSeed])
    json.putPOJO("ElapsedTime", results["ElapsedTime"]!![currentSeed])
    json.putPOJO("Contribution", results["Contribution"]!![currentSeed])
    return json.toString()
  }

  fun toJson(): String {
    val json: ObjectNode = objectMapper.createObjectNode()
    for (i in 0..currentSeed) {
      json.putPOJO("R1Indicator", results["R1Indicator"])
      json.putPOJO("AdditiveEpsilonIndicator", results["AdditiveEpsilonIndicator"])
      json.putPOJO("R2Indicator", results["R2Indicator"])
      json.putPOJO("GenerationalDistance", results["GenerationalDistance"])
      json.putPOJO("Hypervolume", results["Hypervolume"])
      json.putPOJO("Spacing", results["Spacing"])
      json.putPOJO("R3Indicator", results["R3Indicator"])
      json.putPOJO("InvertedGenerationalDistance", results["InvertedGenerationalDistance"])
      json.putPOJO("ElapsedTime", results["ElapsedTime"])
      json.putPOJO("Contribution", results["Contribution"])
    }
    return json.toString()
  }

}