package xyz.re_talk.domain.chat.dto;

import xyz.re_talk.domain.chat.entity.ChatContentType

data class ChatMessageDto(
    val roomId: String,
    val sentAt: String,
    val sender: String,
    val content: String,
    val fingerprint: String,
    val contentType: ChatContentType,
    val isDetailed: Boolean
)