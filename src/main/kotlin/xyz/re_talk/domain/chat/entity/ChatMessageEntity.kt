package xyz.re_talk.domain.chat.entity

import jakarta.persistence.*
import xyz.re_talk.global.common.entity.BaseEntity

@Entity
@Table(
    name = "chat_messages",
    indexes = [
        Index(name = "idx_room_id", columnList = "room_id"),
        Index(name = "idx_mongo_document_id", columnList = "mongo_document_id"),
        Index(name = "idx_sent_at", columnList = "sent_at")
    ]
)
class ChatMessageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "mongo_document_id", nullable = false, length = 24)
    val mongoDocumentId: String,

    @Column(name = "room_id", nullable = false, length = 100)
    val roomId: String,

    @Column(name = "sender", nullable = false, length = 100)
    val sender: String,

    @Column(name = "primary_content_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val primaryContentType: ChatContentType,

    @Column(name = "is_detailed", nullable = false)
    val isDetailed: Boolean,

    @Convert(converter = ChatContentTypeSetConverter::class)
    @Column(name = "content_types", columnDefinition = "TEXT")
    val contentTypes: Set<ChatContentType> = emptySet(),

    @Column(name = "sent_at", nullable = false)
    val sentAt: String,

    @Column(name = "confidence")
    val confidence: Double = 1.0

) : BaseEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChatMessageEntity

        return mongoDocumentId == other.mongoDocumentId
    }

    override fun hashCode(): Int {
        return mongoDocumentId.hashCode()
    }

    override fun toString(): String {
        return "ChatMessageEntity(id=$id, mongoDocumentId='$mongoDocumentId', roomId='$roomId', sender='$sender', primaryContentType=$primaryContentType)"
    }
}
