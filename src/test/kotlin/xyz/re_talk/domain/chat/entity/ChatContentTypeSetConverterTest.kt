package xyz.re_talk.domain.chat.entity

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import xyz.re_talk.global.common.BaseTest

@DisplayName("ChatContentTypeSetConverter 테스트")
class ChatContentTypeSetConverterTest : BaseTest() {

    private val converter = ChatContentTypeSetConverter()

    @Test
    fun `Set을 JSON 배열로 변환한다`() {
        // Given
        val types = setOf(ChatContentType.TEXT, ChatContentType.LINK, ChatContentType.MINI_EMOTICON)

        // When
        val json = converter.convertToDatabaseColumn(types)

        // Then
        assertTrue(json.contains("\"TEXT\""))
        assertTrue(json.contains("\"LINK\""))
        assertTrue(json.contains("\"MINI_EMOTICON\""))
        assertTrue(json.startsWith("["))
        assertTrue(json.endsWith("]"))
    }

    @Test
    fun `빈 Set은 빈 JSON 배열로 변환한다`() {
        // Given
        val emptySet = emptySet<ChatContentType>()

        // When
        val json = converter.convertToDatabaseColumn(emptySet)

        // Then
        assertEquals("[]", json)
    }

    @Test
    fun `null은 빈 JSON 배열로 변환한다`() {
        // When
        val json = converter.convertToDatabaseColumn(null)

        // Then
        assertEquals("[]", json)
    }

    @Test
    fun `JSON 배열을 Set으로 변환한다`() {
        // Given
        val json = "[\"TEXT\",\"LINK\",\"MINI_EMOTICON\"]"

        // When
        val types = converter.convertToEntityAttribute(json)

        // Then
        assertEquals(3, types.size)
        assertTrue(ChatContentType.TEXT in types)
        assertTrue(ChatContentType.LINK in types)
        assertTrue(ChatContentType.MINI_EMOTICON in types)
    }

    @Test
    fun `공백이 있는 JSON 배열도 파싱한다`() {
        // Given
        val json = "[\"TEXT\", \"LINK\", \"MINI_EMOTICON\"]"

        // When
        val types = converter.convertToEntityAttribute(json)

        // Then
        assertEquals(3, types.size)
    }

    @Test
    fun `빈 JSON 배열은 빈 Set으로 변환한다`() {
        // Given
        val json = "[]"

        // When
        val types = converter.convertToEntityAttribute(json)

        // Then
        assertTrue(types.isEmpty())
    }

    @Test
    fun `null은 빈 Set으로 변환한다`() {
        // When
        val types = converter.convertToEntityAttribute(null)

        // Then
        assertTrue(types.isEmpty())
    }

    @Test
    fun `빈 문자열은 빈 Set으로 변환한다`() {
        // When
        val types = converter.convertToEntityAttribute("")

        // Then
        assertTrue(types.isEmpty())
    }

    @Test
    fun `잘못된 enum 값은 무시한다`() {
        // Given
        val json = "[\"TEXT\",\"INVALID_TYPE\",\"LINK\"]"

        // When
        val types = converter.convertToEntityAttribute(json)

        // Then
        assertEquals(2, types.size)
        assertTrue(ChatContentType.TEXT in types)
        assertTrue(ChatContentType.LINK in types)
        assertFalse(types.any { it.name == "INVALID_TYPE" })
    }

    @Test
    fun `변환 왕복 테스트 - 데이터가 보존된다`() {
        // Given
        val original = setOf(ChatContentType.TEXT, ChatContentType.MINI_EMOTICON, ChatContentType.UNICODE_EMOJI)

        // When: Set → JSON → Set
        val json = converter.convertToDatabaseColumn(original)
        val restored = converter.convertToEntityAttribute(json)

        // Then
        assertEquals(original, restored)
    }

    @Test
    fun `모든 ChatContentType을 변환할 수 있다`() {
        // Given
        val allTypes = ChatContentType.entries.toSet()

        // When
        val json = converter.convertToDatabaseColumn(allTypes)
        val restored = converter.convertToEntityAttribute(json)

        // Then
        assertEquals(allTypes, restored)
    }
}

