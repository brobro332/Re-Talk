package xyz.re_talk.domain.chat.document

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "chat_messages")
class ChatMessage(
    @Id
    val id: String? = null,

    val roomId: String,
    val sender: String,
    val content: String,

    @Indexed(unique = true)
    val fingerprint: String,

    val sentAt: LocalDateTime,

    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now()
)

