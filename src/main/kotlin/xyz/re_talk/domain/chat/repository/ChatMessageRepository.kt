package xyz.re_talk.domain.chat.repository

import org.springframework.data.mongodb.repository.MongoRepository
import xyz.re_talk.domain.chat.document.ChatMessage

interface ChatMessageRepository : MongoRepository<ChatMessage, String> {
    fun findByRoomId(roomId: String): List<ChatMessage>
}