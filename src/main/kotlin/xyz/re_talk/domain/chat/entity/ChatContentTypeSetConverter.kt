package xyz.re_talk.domain.chat.entity

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class ChatContentTypeSetConverter : AttributeConverter<Set<ChatContentType>, String> {

    override fun convertToDatabaseColumn(attribute: Set<ChatContentType>?): String {
        if (attribute.isNullOrEmpty()) return "[]"

        val names = attribute.joinToString(",") { "\"${it.name}\"" }
        return "[$names]"
    }

    override fun convertToEntityAttribute(dbData: String?): Set<ChatContentType> {
        if (dbData.isNullOrBlank() || dbData == "[]") return emptySet()

        val trimmed = dbData.trim().removeSurrounding("[", "]")
        if (trimmed.isBlank()) return emptySet()

        return trimmed.split(",")
            .map { it.trim().removeSurrounding("\"") }
            .mapNotNull { name ->
                try {
                    ChatContentType.valueOf(name)
                } catch (e: IllegalArgumentException) {
                    println("⚠️ 알 수 없는 ChatContentType: $name")
                    null
                }
            }
            .toSet()
    }
}

