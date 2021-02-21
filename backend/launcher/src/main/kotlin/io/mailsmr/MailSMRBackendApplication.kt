package io.mailsmr

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["io.mailsmr"])
internal class MailSMRBackendApplication

fun main(args: Array<String>) {
	runApplication<MailSMRBackendApplication>(*args)
}
