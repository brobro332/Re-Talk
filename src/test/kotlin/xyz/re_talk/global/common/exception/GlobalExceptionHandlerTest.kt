package xyz.re_talk.global.common.exception

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.web.bind.annotation.*
import xyz.re_talk.global.common.BaseTest
import xyz.re_talk.global.common.response.ApiResponse
import xyz.re_talk.global.common.response.ErrorCode

@DisplayName("GlobalExceptionHandler 단위 테스트")
class GlobalExceptionHandlerTest : BaseTest() {

    private val handler = GlobalExceptionHandler()

    @Test
    @DisplayName("BusinessException을 처리하여 ApiResponse로 변환")
    fun `BusinessException을 처리하여 ApiResponse로 변환한다`() {
        // given
        val exception = ChatException(
            ErrorCode.CHAT_PARSING_FAILED,
            "파싱 실패 테스트"
        )

        // when
        val response = handler.handleBusinessException(exception)

        // then
        assertEquals(400, response.statusCode.value())
        assertNotNull(response.body)
        assertFalse(response.body!!.success)
        assertEquals("CHAT201", response.body!!.error?.code)
        assertEquals("파싱 실패 테스트", response.body!!.error?.message)
        assertNotNull(response.body!!.traceId)
    }

    @Test
    @DisplayName("일반 예외를 처리하여 500 에러 반환")
    fun `일반 예외를 처리하여 500 에러를 반환한다`() {
        // given
        val exception = RuntimeException("예상치 못한 에러")

        // when
        val response = handler.handleGenericException(exception)

        // then
        assertEquals(500, response.statusCode.value())
        assertNotNull(response.body)
        assertFalse(response.body!!.success)
        assertEquals("COMM004", response.body!!.error?.code)
    }

    @Test
    @DisplayName("details가 있는 BusinessException 처리")
    fun `details가 있는 BusinessException을 처리한다`() {
        // given
        val details = mapOf("field" to "username", "reason" to "too short")
        val exception = BusinessException(
            ErrorCode.COMMON_INVALID_PARAMETER,
            "검증 실패",
            details
        )

        // when
        val response = handler.handleBusinessException(exception)

        // then
        assertNotNull(response.body)
        assertEquals(details, response.body!!.error?.details)
    }
}

@RestController
@RequestMapping("/api/test")
class TestExceptionController {

    @GetMapping("/success")
    fun success(): ApiResponse<String> {
        return ApiResponse.success("테스트 성공", "test-trace-id")
    }

    @GetMapping("/business-error")
    fun businessError() {
        throw ChatException(ErrorCode.CHAT_PARSING_FAILED, "파싱 테스트 실패")
    }

    @GetMapping("/server-error")
    fun serverError() {
        throw RuntimeException("예상치 못한 에러")
    }

    @PostMapping("/echo")
    fun echo(@RequestBody data: Map<String, Any>): ApiResponse<Map<String, Any>> {
        return ApiResponse.success(data)
    }
}

