package xyz.re_talk

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ReTalkApplication

fun main(args: Array<String>) {
	runApplication<ReTalkApplication>(*args)
}
