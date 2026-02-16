package xyz.re_talk.domain.chat.controller;

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import xyz.re_talk.domain.chat.service.ChatParser

@RestController
@RequestMapping("/api/v1/chat")
class ChatController(
    private val chatParser: ChatParser
) {
    @PostMapping("/upload")
    suspend fun uploadChatFile(
        @RequestParam file: MultipartFile,
        @RequestParam roomId: String
    ): ResponseEntity<Map<String, String>> {
        chatParser.parseAndSend(file, roomId)
        return ResponseEntity.ok(mapOf("status" to "success", "message" to "파싱 시작"))
    }
}
