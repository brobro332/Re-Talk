package xyz.re_talk

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class ReTalkApplication

fun main(args: Array<String>) {
	runApplication<ReTalkApplication>(*args)
}
