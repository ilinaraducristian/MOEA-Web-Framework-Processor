package org.moeawebframework.processor

import org.moeaframework.core.spi.AlgorithmFactory
import org.moeaframework.core.spi.ProblemFactory
import org.moeawebframework.processor.moea.CDNAlgorithmProvider
import org.moeawebframework.processor.moea.CDNProblemProvider
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ProcessorApplication

fun main(args: Array<String>) {
	AlgorithmFactory.getInstance().addProvider(CDNAlgorithmProvider())
	ProblemFactory.getInstance().addProvider(CDNProblemProvider())
	runApplication<ProcessorApplication>(*args)
}
