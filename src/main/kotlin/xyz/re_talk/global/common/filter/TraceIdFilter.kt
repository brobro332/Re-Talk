package xyz.re_talk.global.common.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class TraceIdFilter : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(TraceIdFilter::class.java)

    companion object {
        const val TRACE_ID_KEY = "traceId"
        const val TRACE_ID_HEADER = "X-Trace-Id"

        fun getCurrentTraceId(): String {
            return MDC.get(TRACE_ID_KEY) ?: UUID.randomUUID().toString()
        }
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // 1. 클라이언트가 보낸 traceId 확인 (재시도 등의 경우)
        val traceId = request.getHeader(TRACE_ID_HEADER)
            ?: UUID.randomUUID().toString()

        try {
            // 2. MDC에 저장 (이후 모든 로그에 자동 포함)
            MDC.put(TRACE_ID_KEY, traceId)

            // 3. 응답 헤더에 추가
            response.setHeader(TRACE_ID_HEADER, traceId)

            logger.debug("[traceId: $traceId] Request started: ${request.method} ${request.requestURI}")

            // 4. 다음 필터 체인 실행
            filterChain.doFilter(request, response)

        } finally {
            // 5. 요청 완료 후 MDC 정리 (메모리 누수 방지)
            logger.debug("[traceId: $traceId] Request completed")
            MDC.remove(TRACE_ID_KEY)
        }
    }
}

