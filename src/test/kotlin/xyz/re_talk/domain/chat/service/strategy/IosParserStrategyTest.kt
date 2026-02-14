package xyz.re_talk.domain.chat.service.strategy

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DisplayName("IosParserStrategy 단위 테스트")
class IosParserStrategyTest {
    private val parser = IosParserStrategy()

    @Nested
    @DisplayName("isSupport() - 포맷 감지")
    inner class IsSupportTests {

        @Test
        fun `iOS 포맷을 올바르게 감지한다`() {
            // Given
            val line = "2023. 5. 15. 오후 1:26, A : 메시지"

            // When
            val result = parser.isSupport(line)

            // Then
            assertTrue(result, "iOS 포맷(점 구분자 + 오후)을 감지해야 함")
        }

        @Test
        fun `오전 시간대도 감지한다`() {
            // Given
            val line = "2023. 1. 1. 오전 9:00, User : 메시지"

            // When & Then
            assertTrue(parser.isSupport(line))
        }

        @Test
        fun `공백이 없는 포맷도 감지한다`() {
            // Given
            val line = "2023.5.15.오후 3:45, User : 메시지"

            // When & Then
            assertTrue(parser.isSupport(line))
        }

        @Test
        fun `Android 포맷은 거부한다`() {
            // Given - Android는 '년월일' 구분자 사용
            val line = "2023년 5월 15일 오후 1:26, A : 메시지"

            // When
            val result = parser.isSupport(line)

            // Then
            assertFalse(result, "Android 포맷(년월일 구분자)은 거부해야 함")
        }

        @Test
        fun `시간 정보가 없으면 거부한다`() {
            // Given
            val line = "2023. 5. 15. A : 메시지"

            // When & Then
            assertFalse(parser.isSupport(line))
        }

        @Test
        fun `날짜 포맷이 없으면 거부한다`() {
            // Given
            val line = "오후 1:26, A : 메시지"

            // When & Then
            assertFalse(parser.isSupport(line))
        }

        @Test
        fun `앞뒤 공백이 있어도 감지한다`() {
            // Given
            val line = "  2023. 5. 15. 오후 1:26, A : 메시지  "

            // When & Then
            assertTrue(parser.isSupport(line))
        }
    }

    @Nested
    @DisplayName("parse() - 메시지 파싱")
    inner class ParseTests {

        @Test
        fun `정상 메시지를 올바르게 파싱한다`() {
            // Given
            val line = "2023. 5. 15. 오후 1:26, 홍길동 : 안녕하세요"

            // When
            val result = parser.parse(line, "room1")

            // Then
            assertTrue(result is ParserResult.NewMessage)
            val message = result as ParserResult.NewMessage
            assertEquals("홍길동", message.sender)
            assertEquals("2023. 5. 15. 오후 1:26", message.sentAt)
            assertEquals("안녕하세요", message.content)
        }

        @Test
        fun `전각 콜론을 처리한다`() {
            // Given - 전각 콜론(：) 사용
            val line = "2023. 5. 15. 오후 1:26, User： 전각 콜론"

            // When
            val result = parser.parse(line, "room1")

            // Then
            assertTrue(result is ParserResult.NewMessage)
            val message = result as ParserResult.NewMessage
            assertEquals("User", message.sender)
            assertEquals("전각 콜론", message.content)
        }

        @Test
        fun `공백 없는 날짜 포맷도 파싱한다`() {
            // Given
            val line = "2023.5.15.오후 3:45, Tester : 메시지"

            // When
            val result = parser.parse(line, "room1")

            // Then
            assertTrue(result is ParserResult.NewMessage)
            val message = result as ParserResult.NewMessage
            assertEquals("Tester", message.sender)
            assertEquals("2023.5.15.오후 3:45", message.sentAt)
        }

        @Test
        fun `오전 시간대를 파싱한다`() {
            // Given
            val line = "2024. 1. 1. 오전 9:00, 김철수 : 새해 복 많이 받으세요"

            // When
            val result = parser.parse(line, "room1")

            // Then
            assertTrue(result is ParserResult.NewMessage)
            val message = result as ParserResult.NewMessage
            assertEquals("김철수", message.sender)
            assertEquals("2024. 1. 1. 오전 9:00", message.sentAt)
            assertEquals("새해 복 많이 받으세요", message.content)
        }

        @Test
        fun `발신자 이름에 공백이 있어도 파싱한다`() {
            // Given
            val line = "2023. 5. 15. 오후 1:26, 홍 길 동 : 메시지"

            // When
            val result = parser.parse(line, "room1")

            // Then
            assertTrue(result is ParserResult.NewMessage)
            val message = result as ParserResult.NewMessage
            assertEquals("홍 길 동", message.sender)
        }

        @Test
        fun `메시지 내용에 콜론이 있어도 파싱한다`() {
            // Given
            val line = "2023. 5. 15. 오후 1:26, User : 시간은 3:00입니다"

            // When
            val result = parser.parse(line, "room1")

            // Then
            assertTrue(result is ParserResult.NewMessage)
            val message = result as ParserResult.NewMessage
            assertEquals("시간은 3:00입니다", message.content)
        }

        @Test
        fun `날짜 헤더는 Skip한다`() {
            // Given
            val line = "2023년 5월 15일 월요일"

            // When
            val result = parser.parse(line, "room1")

            // Then
            assertEquals(ParserResult.Skip, result)
        }

        @Test
        fun `빈 라인은 Skip한다`() {
            // Given
            val line = "   "

            // When
            val result = parser.parse(line, "room1")

            // Then
            assertEquals(ParserResult.Skip, result)
        }

        @Test
        fun `매칭되지 않는 라인은 AppendToPrev를 반환한다`() {
            // Given - 메시지 패턴이 아닌 일반 텍스트
            val line = "이것은 이전 메시지의 이어지는 내용입니다"

            // When
            val result = parser.parse(line, "room1")

            // Then
            assertEquals(ParserResult.AppendToPrev, result)
        }

        @Test
        fun `특수 문자가 포함된 메시지를 파싱한다`() {
            // Given
            val line = """2023. 5. 15. 오후 1:26, User : !@#$%^&*()_+-=[]{}"""

            // When
            val result = parser.parse(line, "room1")

            // Then
            assertTrue(result is ParserResult.NewMessage)
            val message = result as ParserResult.NewMessage
            assertEquals("""!@#$%^&*()_+-=[]{}""", message.content)
        }

        @Test
        fun `이모지가 포함된 메시지를 파싱한다`() {
            // Given
            val line = "2023. 5. 15. 오후 1:26, User : 안녕하세요 👋😊"

            // When
            val result = parser.parse(line, "room1")

            // Then
            assertTrue(result is ParserResult.NewMessage)
            val message = result as ParserResult.NewMessage
            assertEquals("안녕하세요 👋😊", message.content)
        }

        @Test
        fun `오후 11시 59분 경계값을 파싱한다`() {
            // Given
            val line = "2023. 12. 31. 오후 11:59, User : 마지막 메시지"

            // When
            val result = parser.parse(line, "room1")

            // Then
            assertTrue(result is ParserResult.NewMessage)
            val message = result as ParserResult.NewMessage
            assertEquals("2023. 12. 31. 오후 11:59", message.sentAt)
        }

        @Test
        fun `오전 12시를 파싱한다`() {
            // Given
            val line = "2024. 1. 1. 오전 12:00, User : 새해 첫 메시지"

            // When
            val result = parser.parse(line, "room1")

            // Then
            assertTrue(result is ParserResult.NewMessage)
            val message = result as ParserResult.NewMessage
            assertEquals("2024. 1. 1. 오전 12:00", message.sentAt)
        }
    }
}

