package xyz.re_talk.domain.chat.service

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.mock.web.MockMultipartFile
import xyz.re_talk.domain.chat.dto.ChatMessageDto
import xyz.re_talk.domain.chat.service.strategy.AndroidParserStrategy
import xyz.re_talk.domain.chat.service.strategy.IosParserStrategy
import xyz.re_talk.global.common.BaseTest
import xyz.re_talk.global.util.SecurityUtils
import kotlin.test.assertEquals

@DisplayName("ChatParser 통합 테스트")
class ChatParserTest : BaseTest() {
    private val securityUtils = mockk<SecurityUtils>()
    private val rabbitTemplate = mockk<RabbitTemplate>()
    private val strategies = listOf(AndroidParserStrategy(), IosParserStrategy())
    private lateinit var chatParser: ChatParser

    @BeforeEach
    fun setup() {
        clearAllMocks()
        chatParser = ChatParser(strategies, securityUtils, rabbitTemplate)

        every { securityUtils.encrypt(any()) } returns "encrypted_content"
        every { securityUtils.generateFingerprint(any(), any(), any(), any()) } returns "test_fingerprint"
        every { rabbitTemplate.convertAndSend(any<String>(), any<String>(), any<ChatMessageDto>()) } just Runs
    }

    @Nested
    @DisplayName("Android 카카오톡 파싱")
    inner class AndroidParsingTests {

        @Test
        fun `Android 포맷을 인식하고 멀티라인 메시지를 파싱한다`(): Unit = runBlocking {
            // Given: Android 포맷의 실제 대화 시나리오
            val chatData = """
                2024년 8월 5일 월요일
                2024년 8월 5일 오후 2:30, Alice : 메시지1
                메시지2
                메시지3
                2024년 8월 5일 오후 3:21, Jack : 답장
            """.trimIndent()

            val file = createMockFile("kakaotalk_android.txt", chatData)

            // When: 파일 업로드 → 전략 선택 → 파싱 → 암호화 → 전송
            chatParser.parseAndSend(file, "room1")

            // Then: 전체 흐름 검증
            // 1. 2개 메시지가 올바르게 파싱되어 전송됨
            verify(exactly = 2) {
                rabbitTemplate.convertAndSend("chat.exchange", "chat.parse.routing.key", any<ChatMessageDto>())
            }

            // 2. 첫 번째 메시지 (멀티라인 포함)
            verify {
                rabbitTemplate.convertAndSend(
                    "chat.exchange",
                    "chat.parse.routing.key",
                    match<ChatMessageDto> {
                        it.sender == "Alice" &&
                        it.sentAt == "2024년 8월 5일 오후 2:30" &&
                        it.content == "encrypted_content" &&
                        it.fingerprint == "test_fingerprint"
                    }
                )
            }

            // 3. 두 번째 메시지
            verify {
                rabbitTemplate.convertAndSend(
                    "chat.exchange",
                    "chat.parse.routing.key",
                    match<ChatMessageDto> {
                        it.sender == "Jack" &&
                        it.sentAt == "2024년 8월 5일 오후 3:21"
                    }
                )
            }

            // 4. 암호화와 핑거프린트가 각 메시지마다 호출됨
            verify(exactly = 2) {
                securityUtils.encrypt(any())
                securityUtils.generateFingerprint(any(), any(), any(), any())
            }
        }
    }

    @Nested
    @DisplayName("iOS 카카오톡 파싱")
    inner class IosParsingTests {

        @Test
        fun `기본 메시지를 올바르게 파싱한다`(): Unit = runBlocking {
            // Given
            val chatData = """
                2023년 5월 15일 월요일
                2023. 5. 15. 오후 1:26, Alice : 사진 2장
                2023. 5. 15. 오후 1:37, Jack : 메시지
            """.trimIndent()

            val file = createMockFile("kakaotalk_ios.txt", chatData)

            // When
            chatParser.parseAndSend(file, "room1")

            // Then
            verify(exactly = 2) {
                rabbitTemplate.convertAndSend("chat.exchange", "chat.parse.routing.key", any<ChatMessageDto>())
            }

            verify {
                rabbitTemplate.convertAndSend(any(), any(), match<ChatMessageDto> {
                    it.sender == "Alice" && it.sentAt == "2023. 5. 15. 오후 1:26"
                })
            }

            verify {
                rabbitTemplate.convertAndSend(any(), any(), match<ChatMessageDto> {
                    it.sender == "Jack" && it.sentAt == "2023. 5. 15. 오후 1:37"
                })
            }
        }

        @Test
        fun `전각 콜론을 사용한 메시지를 파싱한다`(): Unit = runBlocking {
            // Given: 전각 콜론(：) 사용
            val chatData = "2023. 5. 15. 오전 10:30, 홍길동： 전각 콜론 테스트"

            val file = createMockFile("fullwidth.txt", chatData)

            // When
            chatParser.parseAndSend(file, "room1")

            // Then
            verify(exactly = 1) {
                rabbitTemplate.convertAndSend(any(), any(), match<ChatMessageDto> {
                    it.sender == "홍길동"
                })
            }
        }

        @Test
        fun `공백 없는 날짜 포맷도 파싱한다`(): Unit = runBlocking {
            // Given
            val chatData = "2023.5.15.오후 3:45, 테스터 : 메시지"

            val file = createMockFile("compact.txt", chatData)

            // When
            chatParser.parseAndSend(file, "room1")

            // Then
            verify(exactly = 1) {
                rabbitTemplate.convertAndSend(any(), any(), match<ChatMessageDto> {
                    it.sender == "테스터" && it.sentAt == "2023.5.15.오후 3:45"
                })
            }
        }

        @Test
        fun `멀티라인 메시지를 올바르게 병합한다`(): Unit = runBlocking {
            // Given
            val chatData = """
                2023. 5. 15. 오후 2:00, Alice : 첫 번째 줄
                두 번째 줄
                세 번째 줄
                2023. 5. 15. 오후 2:01, Bob : 다른 메시지
            """.trimIndent()

            val file = createMockFile("multiline.txt", chatData)

            // When
            chatParser.parseAndSend(file, "room1")

            // Then: 2개 메시지로 파싱되어야 함
            verify(exactly = 2) {
                rabbitTemplate.convertAndSend(any(), any(), any<ChatMessageDto>())
            }

            verify(atLeast = 2) {
                securityUtils.encrypt(any())
            }
        }
    }

    @Nested
    @DisplayName("엣지 케이스")
    inner class EdgeCaseTests {

        @Test
        fun `빈 파일은 예외를 발생시킨다`(): Unit = runBlocking {
            // Given
            val file = createMockFile("empty.txt", "")

            // When & Then: 지원하지 않는 포맷으로 예외 발생
            try {
                chatParser.parseAndSend(file, "room1")
                throw AssertionError("예외가 발생해야 합니다")
            } catch (e: IllegalArgumentException) {
                assertEquals("지원하지 않는 대화 형식입니다.", e.message)
            }

            verify(exactly = 0) {
                rabbitTemplate.convertAndSend(any(), any(), any<ChatMessageDto>())
            }
        }

        @Test
        fun `날짜 헤더만 있는 파일은 예외를 발생시킨다`(): Unit = runBlocking {
            // Given: 날짜 헤더만 있고 실제 메시지는 없음
            val chatData = """
                2024년 1월 1일 월요일
                2024년 1월 2일 화요일
            """.trimIndent()

            val file = createMockFile("headers_only.txt", chatData)

            // When & Then: isSupport가 실패하여 예외 발생
            try {
                chatParser.parseAndSend(file, "room1")
                throw AssertionError("예외가 발생해야 합니다")
            } catch (e: IllegalArgumentException) {
                assertEquals("지원하지 않는 대화 형식입니다.", e.message)
            }

            verify(exactly = 0) {
                rabbitTemplate.convertAndSend(any(), any(), any<ChatMessageDto>())
            }
        }

        @Test
        fun `발신자 이름에 공백이 있어도 파싱한다`(): Unit = runBlocking {
            // Given
            val chatData = "2024년 1월 1일 오후 1:00, 홍 길 동 : 메시지"

            val file = createMockFile("spaces_in_name.txt", chatData)

            // When
            chatParser.parseAndSend(file, "room1")

            // Then
            verify(exactly = 1) {
                rabbitTemplate.convertAndSend(any(), any(), match<ChatMessageDto> {
                    it.sender == "홍 길 동"
                })
            }
        }

        @Test
        fun `특수 문자가 포함된 메시지를 파싱한다`(): Unit = runBlocking {
            // Given
            val chatData = """2024년 1월 1일 오후 5:00, User : !@#$%^&*()_+-=[]{}|;':",.<>?/~`"""

            val file = createMockFile("special_chars.txt", chatData)

            // When
            chatParser.parseAndSend(file, "room1")

            // Then
            verify(exactly = 1) {
                rabbitTemplate.convertAndSend(any(), any(), any<ChatMessageDto>())
            }
        }

        @Test
        fun `카카오톡 미니 이모티콘이 포함된 메시지를 파싱한다`(): Unit = runBlocking {
            // Given
            val chatData = "2024년 1월 1일 오후 6:00, User : 안녕하세요 👋 반갑습니다 😊"

            val file = createMockFile("emoji.txt", chatData)

            // When
            chatParser.parseAndSend(file, "room1")

            // Then
            verify(exactly = 1) {
                rabbitTemplate.convertAndSend(any(), any(), any<ChatMessageDto>())
            }
        }
    }

    @Nested
    @DisplayName("보안 - 암호화 및 핑거프린트")
    inner class SecurityTests {

        @Test
        fun `메시지 내용이 암호화되어 전송된다`(): Unit = runBlocking {
            // Given
            val originalContent = "민감한 메시지 내용"
            val chatData = "2024년 1월 1일 오후 1:00, User : $originalContent"
            val file = createMockFile("encrypt_test.txt", chatData)

            // When: 파싱 → 암호화 → 전송
            chatParser.parseAndSend(file, "room1")

            // Then
            // 1. 원본 내용으로 암호화 호출
            verify {
                securityUtils.encrypt(match { it.contains(originalContent) })
            }

            // 2. 암호화된 내용이 전송됨 (원본이 아님)
            verify {
                rabbitTemplate.convertAndSend(any(), any(), match<ChatMessageDto> {
                    it.content == "encrypted_content" && it.content != originalContent
                })
            }
        }

        @Test
        fun `각 메시지마다 고유한 핑거프린트가 생성된다`(): Unit = runBlocking {
            // Given
            val chatData = """
                2024년 1월 1일 오후 1:00, User1 : 메시지1
                2024년 1월 1일 오후 1:01, User2 : 메시지2
            """.trimIndent()
            val file = createMockFile("fingerprint_test.txt", chatData)

            // When
            chatParser.parseAndSend(file, "room1")

            // Then: 각 메시지마다 핑거프린트 생성
            verify(exactly = 2) {
                securityUtils.generateFingerprint(any(), any(), any(), any())
            }

            verify(exactly = 2) {
                rabbitTemplate.convertAndSend(any(), any(), match<ChatMessageDto> {
                    it.fingerprint == "test_fingerprint"
                })
            }
        }
    }

    private fun createMockFile(filename: String, content: String): MockMultipartFile {
        return MockMultipartFile(
            "file",
            filename,
            "text/plain",
            content.toByteArray(Charsets.UTF_8)
        )
    }
}