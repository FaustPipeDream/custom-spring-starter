package io.github.indexalice.customServiceStarter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@EnableAutoConfiguration
@ConfigurationPropertiesScan(basePackages = "io.github.indexalice.customServiceStarter.config")
class CustomServiceStarterApplication

fun main(args: Array<String>) {
	runApplication<DemoApplication>(*args)
}