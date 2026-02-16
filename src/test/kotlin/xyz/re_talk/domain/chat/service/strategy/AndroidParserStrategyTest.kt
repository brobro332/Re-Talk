package xyz.re_talk.domain.chat.service.strategy

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DisplayName("AndroidParserStrategy 단위 테스트")
class AndroidParserStrategyTest {
    private val parser = AndroidParserStrategy()

    @Nested
    @DisplayName("isSupport() - 포맷 감지")
    inner class IsSupportTests {

        @Test
        fun `Android 포맷을 올바르게 감지한다`() {
            // Given
            val line = "2024년 8월 5일 오후 2:30, A : 메시지"

            // When
            val result = parser.isSupport(line)

            // Then
            assertTrue(result, "Android 포맷(년월일 + 오후)을 감지해야 함")
        }

        @Test
        fun `오전 시간대도 감지한다`() {
            // Given
            val line = "2024년 1월 1일 오전 9:00, User : 메시지"

            // When & Then
            assertTrue(parser.isSupport(line))
        }

        @Test
        fun `공백이 많은 포맷도 감지한다`() {
            // Given
            val line = "2024년  8월  5일  오후  2:30, A : 메시지"

            // When & Then
            assertTrue(parser.isSupport(line))
        }

        @Test
        fun `iOS 포맷은 거부한다`() {
            // Given - iOS는 점(.) 구분자 사용
            val line = "2023. 5. 15. 오후 1:26, A : 메시지"

            // When
            val result = parser.isSupport(line)

            // Then
            assertFalse(result, "iOS 포맷(점 구분자)은 거부해야 함")
        }

        @Test
        fun `시간 정보가 없으면 거부한다`() {
            // Given
            val line = "2024년 8월 5일, A : 메시지"

            // When & Then
            assertFalse(parser.isSupport(line))
        }

        @Test
        fun `년월일이 없으면 거부한다`() {
            // Given
            val line = "오후 2:30, A : 메시지"

            // When & Then
            assertFalse(parser.isSupport(line))
        }

        @Test
        fun `앞뒤 공백이 있어도 감지한다`() {
            // Given
            val line = "  2024년 8월 5일 오후 2:30, A : 메시지  "

            // When & Then
            assertTrue(parser.isSupport(line))
        }

        @Test
        fun `영어 AM PM은 거부한다`() {
            // Given
            val line = "2024년 8월 5일 PM 2:30, A : 메시지"

            // When & Then
            assertFalse(parser.isSupport(line))
        }
    }

    @Nested
    @DisplayName("parse() - 메시지 파싱")
    inner class ParseTests {

        @Test
        fun `정상 메시지를 올바르게 파싱한다`() {
            // Given
            val line = "2024년 8월 5일 오후 2:30, 홍길동 : 안녕하세요"

            // When
            val result = parser.parse(line)

            // Then
            assertTrue(result is ParserResult.NewMessage)
            val message = result as ParserResult.NewMessage
            assertEquals("홍길동", message.sender)
            assertEquals("2024년 8월 5일 오후 2:30", message.sentAt)
            assertEquals("안녕하세요", message.content)
        }

        @Test
        fun `오전 시간대를 파싱한다`() {
            // Given
            val line = "2024년 1월 1일 오전 9:00, 김철수 : 새해 복 많이 받으세요"

            // When
            val result = parser.parse(line)

            // Then
            assertTrue(result is ParserResult.NewMessage)
            val message = result as ParserResult.NewMessage
            assertEquals("김철수", message.sender)
            assertEquals("2024년 1월 1일 오전 9:00", message.sentAt)
            assertEquals("새해 복 많이 받으세요", message.content)
        }

        @Test
        fun `공백이 많은 포맷도 파싱한다`() {
            // Given
            val line = "2024년  8월  5일  오후  11:59,  테스터  :  메시지"

            // When
            val result = parser.parse(line)

            // Then
            assertTrue(result is ParserResult.NewMessage)
            val message = result as ParserResult.NewMessage
            assertEquals("테스터", message.sender)
            assertEquals("2024년  8월  5일  오후  11:59", message.sentAt)
        }

        @Test
        fun `발신자 이름에 공백이 있어도 파싱한다`() {
            // Given
            val line = "2024년 8월 5일 오후 2:30, 홍 길 동 : 메시지"

            // When
            val result = parser.parse(line)

            // Then
            assertTrue(result is ParserResult.NewMessage)
            val message = result as ParserResult.NewMessage
            assertEquals("홍 길 동", message.sender)
        }

        @Test
        fun `메시지 내용에 콜론이 있어도 파싱한다`() {
            // Given
            val line = "2024년 8월 5일 오후 2:30, User : 시간은 3:00입니다"

            // When
            val result = parser.parse(line)

            // Then
            assertTrue(result is ParserResult.NewMessage)
            val message = result as ParserResult.NewMessage
            assertEquals("시간은 3:00입니다", message.content)
        }

        @Test
        fun `메시지 내용에 쉼표가 있어도 파싱한다`() {
            // Given
            val line = "2024년 8월 5일 오후 2:30, User : 안녕, 반가워"

            // When
            val result = parser.parse(line)

            // Then
            assertTrue(result is ParserResult.NewMessage)
            val message = result as ParserResult.NewMessage
            assertEquals("안녕, 반가워", message.content)
        }

        @Test
        fun `날짜 헤더는 Skip한다`() {
            // Given
            val line = "2024년 8월 5일 오후 2:30"

            // When
            val result = parser.parse(line)

            // Then
            assertEquals(ParserResult.Skip, result)
        }

        @Test
        fun `날짜 헤더 변형도 Skip한다`() {
            // Given - 발신자와 메시지 없음
            val line = "2024년 12월 31일 오전 11:59"

            // When
            val result = parser.parse(line)

            // Then
            assertEquals(ParserResult.Skip, result)
        }

        @Test
        fun `빈 라인은 Skip한다`() {
            // Given
            val line = "   "

            // When
            val result = parser.parse(line)

            // Then
            assertEquals(ParserResult.Skip, result)
        }

        @Test
        fun `완전히 빈 문자열도 Skip한다`() {
            // Given
            val line = ""

            // When
            val result = parser.parse(line)

            // Then
            assertEquals(ParserResult.Skip, result)
        }

        @Test
        fun `매칭되지 않는 라인은 AppendToPrev를 반환한다`() {
            // Given - 메시지 패턴이 아닌 일반 텍스트
            val line = "이것은 이전 메시지의 이어지는 내용입니다"

            // When
            val result = parser.parse(line)

            // Then
            assertEquals(ParserResult.AppendToPrev, result)
        }

        @Test
        fun `특수 문자가 포함된 메시지를 파싱한다`() {
            // Given
            val line = """2024년 8월 5일 오후 2:30, User : !@#${'$'}%^&*()_+-=[]{}"""

            // When
            val result = parser.parse(line)

            // Then
            assertTrue(result is ParserResult.NewMessage)
            val message = result as ParserResult.NewMessage
            assertEquals("""!@#${'$'}%^&*()_+-=[]{}""", message.content)
        }

        @Test
        fun `카카오톡 미니 이모티콘이 포함된 메시지를 파싱한다`() {
            // Given
            val line = "2024년 8월 5일 오후 2:30, User : 안녕하세요 👋😊"

            // When
            val result = parser.parse(line)

            // Then
            assertTrue(result is ParserResult.NewMessage)
            val message = result as ParserResult.NewMessage
            assertEquals("안녕하세요 👋😊", message.content)
        }

        @Test
        fun `오후 11시 59분 경계값을 파싱한다`() {
            // Given
            val line = "2024년 12월 31일 오후 11:59, User : 마지막 메시지"

            // When
            val result = parser.parse(line)

            // Then
            assertTrue(result is ParserResult.NewMessage)
            val message = result as ParserResult.NewMessage
            assertEquals("2024년 12월 31일 오후 11:59", message.sentAt)
        }

        @Test
        fun `오전 12시를 파싱한다`() {
            // Given
            val line = "2024년 1월 1일 오전 12:00, User : 자정 메시지"

            // When
            val result = parser.parse(line)

            // Then
            assertTrue(result is ParserResult.NewMessage)
            val message = result as ParserResult.NewMessage
            assertEquals("2024년 1월 1일 오전 12:00", message.sentAt)
        }

        @Test
        fun `한 자리 월일을 파싱한다`() {
            // Given
            val line = "2024년 1월 5일 오전 9:05, User : 테스트"

            // When
            val result = parser.parse(line)

            // Then
            assertTrue(result is ParserResult.NewMessage)
            val message = result as ParserResult.NewMessage
            assertEquals("2024년 1월 5일 오전 9:05", message.sentAt)
        }

        @Test
        fun `두 자리 월일을 파싱한다`() {
            // Given
            val line = "2024년 12월 25일 오후 10:30, User : 크리스마스"

            // When
            val result = parser.parse(line)

            // Then
            assertTrue(result is ParserResult.NewMessage)
            val message = result as ParserResult.NewMessage
            assertEquals("2024년 12월 25일 오후 10:30", message.sentAt)
            assertEquals("크리스마스", message.content)
        }

        @Test
        fun `빈 메시지 내용도 파싱한다`() {
            // Given - 발신자는 있지만 메시지 없음 (사진/파일 전송 등)
            val line = "2024년 8월 5일 오후 2:30, User : "

            // When
            val result = parser.parse(line)

            // Then
            assertTrue(result is ParserResult.NewMessage)
            val message = result as ParserResult.NewMessage
            assertEquals("", message.content)
        }

        @Test
        fun `발신자 이름에 특수문자가 있어도 파싱한다`() {
            // Given
            val line = "2024년 8월 5일 오후 2:30, User_123 : 메시지"

            // When
            val result = parser.parse(line)

            // Then
            assertTrue(result is ParserResult.NewMessage)
            val message = result as ParserResult.NewMessage
            assertEquals("User_123", message.sender)
        }

        @Test
        fun `긴 메시지도 파싱한다`() {
            // Given
            val longContent = "A".repeat(1000)
            val line = "2024년 8월 5일 오후 2:30, User : $longContent"

            // When
            val result = parser.parse(line)

            // Then
            assertTrue(result is ParserResult.NewMessage)
            val message = result as ParserResult.NewMessage
            assertEquals(longContent, message.content)
        }
    }

    @Nested
    @DisplayName("엣지 케이스 및 경계값 테스트")
    inner class EdgeCaseTests {

        @Test
        fun `윤년 2월 29일을 파싱한다`() {
            // Given
            val line = "2024년 2월 29일 오전 10:00, User : 윤년"

            // When
            val result = parser.parse(line)

            // Then
            assertTrue(result is ParserResult.NewMessage)
            val message = result as ParserResult.NewMessage
            assertEquals("2024년 2월 29일 오전 10:00", message.sentAt)
        }

        @Test
        fun `연도 시작일을 파싱한다`() {
            // Given
            val line = "2024년 1월 1일 오전 12:00, User : 새해"

            // When
            val result = parser.parse(line)

            // Then
            assertTrue(result is ParserResult.NewMessage)
        }

        @Test
        fun `연도 마지막일을 파싱한다`() {
            // Given
            val line = "2024년 12월 31일 오후 11:59, User : 송년"

            // When
            val result = parser.parse(line)

            // Then
            assertTrue(result is ParserResult.NewMessage)
        }

        @Test
        fun `오전 1시를 파싱한다`() {
            // Given
            val line = "2024년 8월 5일 오전 1:00, User : 새벽"

            // When
            val result = parser.parse(line)

            // Then
            assertTrue(result is ParserResult.NewMessage)
        }

        @Test
        fun `정오를 파싱한다`() {
            // Given
            val line = "2024년 8월 5일 오후 12:00, User : 점심"

            // When
            val result = parser.parse(line)

            // Then
            assertTrue(result is ParserResult.NewMessage)
            val message = result as ParserResult.NewMessage
            assertEquals("2024년 8월 5일 오후 12:00", message.sentAt)
        }

        @Test
        fun `URL이 포함된 메시지를 파싱한다`() {
            // Given
            val line = "2024년 8월 5일 오후 2:30, User : https://example.com"

            // When
            val result = parser.parse(line)

            // Then
            assertTrue(result is ParserResult.NewMessage)
            val message = result as ParserResult.NewMessage
            assertEquals("https://example.com", message.content)
        }

        @Test
        fun `줄바꿈 문자는 Skip으로 처리`() {
            // Given
            val line = "\n"

            // When
            val result = parser.parse(line)

            // Then: trim 후 빈 문자열이 되므로 Skip
            assertEquals(ParserResult.Skip, result)
        }

        @Test
        fun `탭 문자가 있는 라인은 AppendToPrev`() {
            // Given
            val line = "\t일반 텍스트"

            // When
            val result = parser.parse(line)

            // Then
            assertEquals(ParserResult.AppendToPrev, result)
        }
    }
}

