package org.moeawebframework.processor

import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ProcessorApplication

fun main(args: Array<String>) {
  runApplication<ProcessorApplication>(*args) {
    webApplicationType = WebApplicationType.NONE
  }
}