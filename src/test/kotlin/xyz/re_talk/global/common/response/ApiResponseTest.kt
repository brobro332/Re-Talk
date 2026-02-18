package xyz.re_talk.global.common.response

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import xyz.re_talk.global.common.BaseTest

@DisplayName("ApiResponse 테스트")
class ApiResponseTest : BaseTest() {

    @Test
    @DisplayName("성공 응답 생성 - 데이터 있음")
    fun `success response with data`() {
        // given
        val data = mapOf("key" to "value")

        // when
        val response = ApiResponse.success(data)

        // then
        assertTrue(response.success)
        assertEquals(data, response.data)
        assertNull(response.error)
        assertNotNull(response.traceId)
        assertNotNull(response.timestamp)
    }

    @Test
    @DisplayName("성공 응답 생성 - 데이터 없음")
    fun `success response without data`() {
        // when
        val response = ApiResponse.success()

        // then
        assertTrue(response.success)
        assertNotNull(response.data)  // Unit
        assertNull(response.error)
        assertNotNull(response.traceId)
    }

    @Test
    @DisplayName("실패 응답 생성 - ErrorCode 사용")
    fun `fail response with error code`() {
        // given
        val errorCode = ErrorCode.COMMON_INVALID_PARAMETER
        val customMessage = "커스텀 에러 메시지"
        val details = mapOf("field" to "username")

        // when
        val response = ApiResponse.fail<Unit>(
            errorCode = errorCode,
            message = customMessage,
            details = details
        )

        // then
        assertFalse(response.success)
        assertNull(response.data)
        assertNotNull(response.error)
        assertEquals(errorCode.code, response.error?.code)
        assertEquals(customMessage, response.error?.message)
        assertEquals(details, response.error?.details)
    }

    @Test
    @DisplayName("실패 응답 생성 - ErrorCode 기본 메시지 사용")
    fun `fail response with default message`() {
        // given
        val errorCode = ErrorCode.CHAT_PARSING_FAILED

        // when
        val response = ApiResponse.fail<Unit>(errorCode = errorCode)

        // then
        assertFalse(response.success)
        assertEquals(errorCode.code, response.error?.code)
        assertEquals(errorCode.message, response.error?.message)
        assertNull(response.error?.details)
    }

    @Test
    @DisplayName("실패 응답 생성 - ErrorInfo 직접 사용")
    fun `fail response with error info`() {
        // given
        val errorInfo = ErrorInfo(
            code = "CUSTOM_001",
            message = "커스텀 에러",
            details = mapOf("reason" to "test")
        )

        // when
        val response = ApiResponse.fail<Unit>(errorInfo = errorInfo)

        // then
        assertFalse(response.success)
        assertEquals(errorInfo, response.error)
    }

    @Test
    @DisplayName("traceId가 고유한지 확인")
    fun `traceId should be unique`() {
        // when
        val response1 = ApiResponse.success("data1")
        val response2 = ApiResponse.success("data2")

        // then
        assertNotEquals(response1.traceId, response2.traceId)
    }
}




