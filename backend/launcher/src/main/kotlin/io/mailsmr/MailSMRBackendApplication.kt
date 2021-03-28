package io.mailsmr

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["io.mailsmr"])
@EnableScheduling
internal class MailSMRBackendApplication

fun main(args: Array<String>) {
    runApplication<MailSMRBackendApplication>(*args)
}
