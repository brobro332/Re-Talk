package xyz.re_talk.domain.chat.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import xyz.re_talk.domain.chat.repository.ChatMessageRepository
import xyz.re_talk.domain.chat.repository.ChatMessageEntityRepository
import xyz.re_talk.global.common.BaseTest
import java.time.LocalDateTime

@DisplayName("ChatMessageConsumer - 날짜 파싱 테스트")
class ChatMessageConsumerDateParseTest : BaseTest() {

    private val mongoRepository = mock(ChatMessageRepository::class.java)
    private val postgresRepository = mock(ChatMessageEntityRepository::class.java)

    private val consumer = ChatMessageConsumer(mongoRepository, postgresRepository)

    @Test
    fun `Android 형식 날짜를 파싱한다`() {
        // Given
        val dateStr = "2024년 1월 15일 오후 3:30"

        // When
        val result = consumer.javaClass.getDeclaredMethod("parseSentAt", String::class.java).apply {
            isAccessible = true
        }.invoke(consumer, dateStr) as LocalDateTime

        // Then
        assertEquals(2024, result.year)
        assertEquals(1, result.monthValue)
        assertEquals(15, result.dayOfMonth)
        assertEquals(15, result.hour) // 오후 3시 = 15시
        assertEquals(30, result.minute)
    }

    @Test
    fun `iOS 형식 날짜를 파싱한다`() {
        // Given
        val dateStr = "2024. 1. 15. 오전 9:45"

        // When
        val result = consumer.javaClass.getDeclaredMethod("parseSentAt", String::class.java).apply {
            isAccessible = true
        }.invoke(consumer, dateStr) as LocalDateTime

        // Then
        assertEquals(2024, result.year)
        assertEquals(1, result.monthValue)
        assertEquals(15, result.dayOfMonth)
        assertEquals(9, result.hour)
        assertEquals(45, result.minute)
    }

    @Test
    fun `오전 12시는 00시로 변환된다`() {
        // Given
        val dateStr = "2024년 1월 1일 오전 12:00"

        // When
        val result = consumer.javaClass.getDeclaredMethod("parseSentAt", String::class.java).apply {
            isAccessible = true
        }.invoke(consumer, dateStr) as LocalDateTime

        // Then
        assertEquals(0, result.hour) // 오전 12시 = 자정 = 0시
    }

    @Test
    fun `오후 12시는 12시로 유지된다`() {
        // Given
        val dateStr = "2024년 1월 1일 오후 12:00"

        // When
        val result = consumer.javaClass.getDeclaredMethod("parseSentAt", String::class.java).apply {
            isAccessible = true
        }.invoke(consumer, dateStr) as LocalDateTime

        // Then
        assertEquals(12, result.hour) // 오후 12시 = 정오 = 12시
    }

    @Test
    fun `다양한 Android 날짜 형식을 파싱한다`() {
        val testCases = mapOf(
            "2024년 1월 1일 오전 1:00" to 1,
            "2024년 1월 1일 오전 11:00" to 11,
            "2024년 1월 1일 오후 1:00" to 13,
            "2024년 1월 1일 오후 11:00" to 23
        )

        testCases.forEach { (dateStr, expectedHour) ->
            val result = consumer.javaClass.getDeclaredMethod("parseSentAt", String::class.java).apply {
                isAccessible = true
            }.invoke(consumer, dateStr) as LocalDateTime

            assertEquals(expectedHour, result.hour, "실패: $dateStr")
        }
    }

    @Test
    fun `잘못된 형식은 현재 시간을 반환한다`() {
        // Given
        val invalidDateStr = "invalid date format"

        // When
        val result = consumer.javaClass.getDeclaredMethod("parseSentAt", String::class.java).apply {
            isAccessible = true
        }.invoke(consumer, invalidDateStr) as LocalDateTime

        // Then - 현재 시간 근처여야 함 (1분 이내)
        val now = LocalDateTime.now()
        assertTrue(result.isAfter(now.minusMinutes(1)))
        assertTrue(result.isBefore(now.plusMinutes(1)))
    }
}

