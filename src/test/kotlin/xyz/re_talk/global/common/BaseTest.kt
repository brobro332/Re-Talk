package xyz.re_talk.global.common

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.slf4j.MDC
import java.util.*

abstract class BaseTest {

    @BeforeEach
    fun setUpTraceId() {
        val traceId = UUID.randomUUID().toString()
        MDC.put("traceId", traceId)
    }

    @AfterEach
    fun clearTraceId() {
        MDC.clear()
    }
}

