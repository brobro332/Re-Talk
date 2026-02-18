package xyz.re_talk.global.common.exception

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import xyz.re_talk.global.common.BaseTest
import xyz.re_talk.global.common.response.ErrorCode

@DisplayName("BusinessException 테스트")
class BusinessExceptionTest : BaseTest() {

    @Test
    @DisplayName("기본 메시지로 예외 생성")
    fun `기본 메시지로 예외를 생성한다`() {
        // given
        val errorCode = ErrorCode.CHAT_PARSING_FAILED

        // when
        val exception = BusinessException(errorCode)

        // then
        assertEquals(errorCode, exception.errorCode)
        assertEquals(errorCode.message, exception.message)
        assertEquals(errorCode.message, exception.getDisplayMessage())
        assertNull(exception.details)
    }

    @Test
    @DisplayName("커스텀 메시지로 예외 생성")
    fun `커스텀 메시지로 예외를 생성한다`() {
        // given
        val errorCode = ErrorCode.CHAT_PARSING_FAILED
        val customMessage = "파싱 중 예상치 못한 오류 발생"

        // when
        val exception = BusinessException(errorCode, customMessage)

        // then
        assertEquals(errorCode, exception.errorCode)
        assertEquals(customMessage, exception.message)
        assertEquals(customMessage, exception.getDisplayMessage())
    }

    @Test
    @DisplayName("상세 정보와 함께 예외 생성")
    fun `상세 정보와 함께 예외를 생성한다`() {
        // given
        val errorCode = ErrorCode.COMMON_INVALID_PARAMETER
        val details = mapOf(
            "field" to "username",
            "value" to "invalid-name"
        )

        // when
        val exception = BusinessException(errorCode, details = details)

        // then
        assertEquals(details, exception.details)
    }

    @Test
    @DisplayName("ChatException 생성")
    fun `ChatException을 생성한다`() {
        // given
        val errorCode = ErrorCode.CHAT_DUPLICATE_MESSAGE

        // when
        val exception = ChatException(errorCode)

        // then
        assertTrue(exception is BusinessException)
        assertEquals(errorCode, exception.errorCode)
    }

    @Test
    @DisplayName("FileException 생성")
    fun `FileException을 생성한다`() {
        // given
        val errorCode = ErrorCode.FILE_SIZE_EXCEEDED
        val customMessage = "파일 크기가 10MB를 초과했습니다."

        // when
        val exception = FileException(errorCode, customMessage)

        // then
        assertTrue(exception is BusinessException)
        assertEquals(customMessage, exception.getDisplayMessage())
    }

    @Test
    @DisplayName("DatabaseException 생성")
    fun `DatabaseException을 생성한다`() {
        // given
        val errorCode = ErrorCode.DB_DUPLICATE_KEY
        val details = mapOf("constraint" to "uk_fingerprint")

        // when
        val exception = DatabaseException(errorCode, details = details)

        // then
        assertTrue(exception is BusinessException)
        assertEquals(details, exception.details)
    }
}

