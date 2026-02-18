package xyz.re_talk.domain.chat.entity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import xyz.re_talk.global.common.BaseTest

class ChatContentTypeQuickTest : BaseTest() {

    @Test
    fun `jpg와 jpeg 모두 PHOTO로 인식된다`() {
        val jpg = ChatContentType.analyze("image.jpg")
        val jpeg = ChatContentType.analyze("photo.jpeg")

        assertEquals(ChatContentType.PHOTO, jpg.primaryType)
        assertEquals(ChatContentType.PHOTO, jpeg.primaryType)
        assertTrue(jpg.isDetailed)
        assertTrue(jpeg.isDetailed)
    }

    @Test
    fun `텍스트 사진과 파일명 사진을 구분한다`() {
        val text = ChatContentType.analyze("사진")
        val file = ChatContentType.analyze("KakaoTalk_123.jpg")

        assertEquals(ChatContentType.PHOTO, text.primaryType)
        assertEquals(ChatContentType.PHOTO, file.primaryType)

        assertFalse(text.isDetailed, "텍스트는 상세정보 없음")
        assertTrue(file.isDetailed, "파일명은 상세정보 있음")
    }

    @Test
    fun `모든 확장자가 제대로 등록되어 있다`() {
        val testCases = mapOf(
            "jpg" to ChatContentType.PHOTO,
            "jpeg" to ChatContentType.PHOTO,
            "png" to ChatContentType.PHOTO,
            "mp4" to ChatContentType.VIDEO,
            "mp3" to ChatContentType.VOICE,
            "pdf" to ChatContentType.FILE
        )

        testCases.forEach { (ext, expectedType) ->
            val result = ChatContentType.fromExtension(ext)
            assertEquals(expectedType, result, "확장자 $ext 실패")
        }
    }

    @Test
    fun `실제 시나리오 - 텍스트 사진과 파일명 모두 PHOTO다`() {
        val textPhoto = ChatContentType.analyze("사진")
        assertEquals(ChatContentType.PHOTO, textPhoto.primaryType)
        assertFalse(textPhoto.isDetailed, "텍스트 '사진'은 상세정보 없음")

        val filePhoto = ChatContentType.analyze("1@#32432424.jpg")
        assertEquals(ChatContentType.PHOTO, filePhoto.primaryType)
        assertTrue(filePhoto.isDetailed, "파일명은 상세정보 있음")

        println("✅ '사진' → PHOTO (isDetailed=${textPhoto.isDetailed})")
        println("✅ 'KakaoTalk_002.jpg' → PHOTO (isDetailed=${filePhoto.isDetailed})")
    }

    @Test
    fun `다양한 파일명 패턴도 인식한다`() {
        val filenames = listOf(
            "KakaoTalk_002.jpg",
            "KakaoTalk_20240101_143025.jpeg",
            "IMG_001.png",
            "사진_123.jpg",
            "photo-final-v2.jpg"
        )

        filenames.forEach { filename ->
            val analysis = ChatContentType.analyze(filename)
            assertEquals(ChatContentType.PHOTO, analysis.primaryType, "실패: $filename")
            assertTrue(analysis.isDetailed, "${filename}은 파일명이므로 상세정보 있음")
        }
    }
}

