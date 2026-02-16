package xyz.re_talk.domain.chat.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import xyz.re_talk.domain.chat.dto.ChatMessageDto
import xyz.re_talk.domain.chat.entity.ChatContentType
import xyz.re_talk.domain.chat.service.strategy.ChatParserStrategy
import xyz.re_talk.domain.chat.service.strategy.ParserResult
import xyz.re_talk.domain.chat.service.strategy.PendingMessage
import xyz.re_talk.global.util.SecurityUtils

@Component
class ChatParser(
    private val strategies: List<ChatParserStrategy>,
    private val securityUtils: SecurityUtils,
    private val rabbitTemplate: RabbitTemplate
) {
    suspend fun parseAndSend(file: MultipartFile, roomId: String) = withContext(Dispatchers.IO) {
        val sampleLines = file.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
            (1..10).mapNotNull { reader.readLine() }
        }

        val strategy = strategies.find { s ->
            sampleLines.any { s.isSupport(it) }
        } ?: throw IllegalArgumentException("지원하지 않는 대화 형식입니다.")

        println("선택된 전략: ${strategy::class.simpleName}")

        file.inputStream.bufferedReader(Charsets.UTF_8).useLines { lines ->
            var currentMsg: PendingMessage? = null

            lines.forEach { line ->
                when (val result = strategy.parse(line)) {
                    is ParserResult.NewMessage -> {
                        currentMsg?.let { processAndSend(it) }
                        currentMsg = PendingMessage(
                            roomId = roomId,
                            sentAt = result.sentAt,
                            sender = result.sender,
                            contentBuffer = StringBuilder(result.content)
                        )
                    }
                    is ParserResult.AppendToPrev -> {
                        currentMsg?.appendContent(line)
                    }
                    is ParserResult.Skip -> { /* 생략 */ }
                }
            }
            currentMsg?.let { processAndSend(it) }
        }
    }

    private fun processAndSend(msg: PendingMessage) {
        val finalContent = msg.contentBuffer.toString().trim()
        if (finalContent.isEmpty()) return

        val analysis = ChatContentType.analyze(finalContent)

        val messageDto = ChatMessageDto(
            roomId = msg.roomId,
            sentAt = msg.sentAt,
            sender = msg.sender,
            content = securityUtils.encrypt(finalContent),
            fingerprint = securityUtils.generateFingerprint(msg.roomId, msg.sentAt, msg.sender, finalContent),
            primaryContentType = analysis.primaryType,
            isDetailed = analysis.isDetailed,
            contentTypes = analysis.types
        )
        rabbitTemplate.convertAndSend("chat.exchange", "chat.parse.routing.key", messageDto)
    }
}

