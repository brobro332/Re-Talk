package xyz.re_talk.domain.chat.entity

enum class ChatContentType(
    val description: String,
    val isDetailedByDefault: Boolean
) {
    TEXT("일반 텍스트 메시지", false),
    PHOTO("사진 파일", true),
    VIDEO("동영상 파일", true),
    FILE("기타 문서 파일", true),
    EMOJI("괄호형 이모지", true),
    EMOTICON("카카오 이모티콘", true),
    SYSTEM("시스템 메시지", false);

    companion object {
        fun classify(content: String): Pair<ChatContentType, Boolean> {
            return when {
                content.endsWith(".jpg", true) || content.endsWith(".png", true) -> PHOTO to true
                content.endsWith(".mp4", true) || content.endsWith(".mov", true) -> VIDEO to true
                content.contains(Regex("""\.(pdf|zip|docx|xlsx|txt)$""")) -> FILE to true

                content == "사진" -> PHOTO to false
                content == "동영상" -> VIDEO to false
                content == "파일" -> FILE to false

                content.matches(Regex("""^\(.*\)$""")) -> EMOJI to true
                content == "이모티콘" -> EMOTICON to true

                content.contains("님이 들어왔습니다.") || content.contains("님이 나갔습니다.") -> SYSTEM to false

                else -> TEXT to false
            }
        }
    }
}