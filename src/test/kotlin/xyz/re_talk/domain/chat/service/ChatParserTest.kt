package xyz.re_talk.domain.chat.service

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.mock.web.MockMultipartFile
import xyz.re_talk.domain.chat.dto.ChatMessageDto
import xyz.re_talk.domain.chat.service.strategy.AndroidParserStrategy
import xyz.re_talk.domain.chat.service.strategy.IosParserStrategy
import xyz.re_talk.global.util.SecurityUtils

class ChatParserTest {
    private val securityUtils = mockk<SecurityUtils>()
    private val rabbitTemplate = mockk<RabbitTemplate>()

    private val strategies = listOf(AndroidParserStrategy(), IosParserStrategy())
    private val chatParser = ChatParser(strategies, securityUtils, rabbitTemplate)

    @Test
    fun `안드로이드 카톡 텍스트 파싱 및 전송 테스트`(): Unit = runBlocking {
        // Given
        val chatData = """
            2024년 8월 5일 월요일
            2024년 8월 5일 오후 2:30, A : 메시지1
            메시지2
            메시지3
            2024년 8월 5일 오후 3:21, B : 답장
        """.trimIndent()

        val file = MockMultipartFile("file", "kakaotalk_android.txt", "text/plain", chatData.toByteArray(Charsets.UTF_8))
        val roomId = "room1"

        every { securityUtils.encrypt(any()) } returns "encrypted_content"
        every { securityUtils.generateFingerprint(any(), any(), any(), any()) } returns "fingerprint"
        every { rabbitTemplate.convertAndSend(any<String>(), any<String>(), any<ChatMessageDto>()) } just Runs

        // When
        chatParser.parseAndSend(file, roomId)

        // Then
        verify(exactly = 2) {
            rabbitTemplate.convertAndSend("chat.exchange", "chat.parse.routing.key", any<ChatMessageDto>())
        }
    }

    @Test
    fun `아이폰 카톡 텍스트 파싱 테스트`(): Unit = runBlocking {
        // Given
        val chatData = """
            2023년 5월 15일 월요일
            2023. 5. 15. 오후 1:26, A : 사진 2장
            2023. 5. 15. 오후 1:37, B : 메시지
        """.trimIndent()

        val file = MockMultipartFile("file", "kakaotalk_ios.txt", "text/plain", chatData.toByteArray(Charsets.UTF_8))
        val roomId = "room1"

        every { securityUtils.encrypt(any()) } returns "encrypted"
        every { securityUtils.generateFingerprint(any(), any(), any(), any()) } returns "fp"
        every { rabbitTemplate.convertAndSend(any(), any(), any<ChatMessageDto>()) } just Runs

        // When
        chatParser.parseAndSend(file, roomId)

        // Then
        verify(exactly = 2) {
            rabbitTemplate.convertAndSend(any(), any(), any<ChatMessageDto>())
        }
    }
}