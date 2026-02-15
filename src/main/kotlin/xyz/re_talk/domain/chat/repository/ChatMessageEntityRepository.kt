package xyz.re_talk.domain.chat.repository

import org.springframework.data.jpa.repository.JpaRepository
import xyz.re_talk.domain.chat.entity.ChatMessageEntity

interface ChatMessageEntityRepository : JpaRepository<ChatMessageEntity, Long> {
    fun findByRoomId(roomId: String): List<ChatMessageEntity>
    fun findByMongoDocumentId(mongoDocumentId: String): ChatMessageEntity?
    fun existsByMongoDocumentId(mongoDocumentId: String): Boolean
}

