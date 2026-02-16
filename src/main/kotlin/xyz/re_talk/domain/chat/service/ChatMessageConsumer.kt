package xyz.re_talk.domain.chat.service

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import xyz.re_talk.domain.chat.document.ChatMessage
import xyz.re_talk.domain.chat.dto.ChatMessageDto
import xyz.re_talk.domain.chat.entity.ChatMessageEntity
import xyz.re_talk.domain.chat.repository.ChatMessageEntityRepository
import xyz.re_talk.domain.chat.repository.ChatMessageRepository
import xyz.re_talk.global.config.RabbitMqConfig
import java.time.LocalDateTime

@Component
class ChatMessageConsumer(
    private val mongoRepository: ChatMessageRepository,
    private val postgresRepository: ChatMessageEntityRepository
) {

    @RabbitListener(queues = [RabbitMqConfig.QUEUE])
    @Transactional
    fun consume(dto: ChatMessageDto) {
        try {
            val mongoDocument = ChatMessage(
                roomId = dto.roomId,
                sender = dto.sender,
                content = dto.content,
                fingerprint = dto.fingerprint,
                sentAt = parseSentAt(dto.sentAt)
            )

            val savedDocument = try {
                mongoRepository.save(mongoDocument)
            } catch (e: Exception) {
                if (e.message?.contains("duplicate key") == true) {
                    println("⚠️ 중복 메시지 감지 (fingerprint: ${dto.fingerprint})")
                    return
                }
                throw e
            }

            val postgresEntity = ChatMessageEntity(
                mongoDocumentId = savedDocument.id!!,
                roomId = dto.roomId,
                sender = dto.sender,
                primaryContentType = dto.primaryContentType,
                isDetailed = dto.isDetailed,
                contentTypes = dto.contentTypes,
                sentAt = dto.sentAt
            )

            postgresRepository.save(postgresEntity)
        } catch (e: Exception) {
            println("❌ 메시지 저장 실패: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    private fun parseSentAt(sentAtStr: String): LocalDateTime {
        return try {
            if (sentAtStr.contains("년")) {
                parseAndroidFormat(sentAtStr)
            } else if (sentAtStr.contains(".")) {
                parseIosFormat(sentAtStr)
            } else {
                println("알 수 없는 날짜 형식: $sentAtStr")
                LocalDateTime.now()
            }
        } catch (e: Exception) {
            println("❌ 날짜 파싱 실패: $sentAtStr - ${e.message}")
            LocalDateTime.now()
        }
    }

    private fun parseAndroidFormat(dateStr: String): LocalDateTime {
        val pattern = """(\d{4})년\s*(\d{1,2})월\s*(\d{1,2})일\s*(오전|오후)\s*(\d{1,2}):(\d{2})""".toRegex()
        val match = pattern.find(dateStr) ?: throw IllegalArgumentException("Android 형식 매칭 실패: $dateStr")

        val (year, month, day, amPm, hour, minute) = match.destructured

        val hour24 = convertTo24Hour(hour.toInt(), amPm)

        return LocalDateTime.of(
            year.toInt(),
            month.toInt(),
            day.toInt(),
            hour24,
            minute.toInt()
        )
    }

    /**
     * iOS 형식 파싱: "2024. 1. 1. 오후 1:00"
     */
    private fun parseIosFormat(dateStr: String): LocalDateTime {
        val pattern = """(\d{4})\.\s*(\d{1,2})\.\s*(\d{1,2})\.\s*(오전|오후)\s*(\d{1,2}):(\d{2})""".toRegex()
        val match = pattern.find(dateStr) ?: throw IllegalArgumentException("iOS 형식 매칭 실패: $dateStr")

        val (year, month, day, amPm, hour, minute) = match.destructured

        val hour24 = convertTo24Hour(hour.toInt(), amPm)

        return LocalDateTime.of(
            year.toInt(),
            month.toInt(),
            day.toInt(),
            hour24,
            minute.toInt()
        )
    }

    /**
     * 오전/오후를 24시간 형식으로 변환
     *
     * @param hour 12시간 형식 (1-12)
     * @param amPm "오전" 또는 "오후"
     * @return 24시간 형식 (0-23)
     */
    private fun convertTo24Hour(hour: Int, amPm: String): Int {
        return when {
            amPm == "오전" && hour == 12 -> 0       // 오전 12시 = 00시 (자정)
            amPm == "오전" -> hour                  // 오전 1-11시 = 1-11시
            amPm == "오후" && hour == 12 -> 12      // 오후 12시 = 12시 (정오)
            amPm == "오후" -> hour + 12             // 오후 1-11시 = 13-23시
            else -> throw IllegalArgumentException("잘못된 시간: $hour $amPm")
        }
    }
}