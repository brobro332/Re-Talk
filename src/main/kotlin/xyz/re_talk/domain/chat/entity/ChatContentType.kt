package xyz.re_talk.domain.chat.entity

data class ContentAnalysis(
    val types: Set<ChatContentType>,
    val primaryType: ChatContentType,
    val isDetailed: Boolean,
    val confidence: Double = 1.0
)

enum class ChatContentType(
    val description: String,
    val isDetailedByDefault: Boolean,
    val fileExtensions: List<String> = emptyList()
) {
    TEXT("일반 텍스트 메시지", false, emptyList()),
    PHOTO("사진 파일", true, listOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic")),
    VIDEO("동영상 파일", true, listOf("mp4", "avi", "mov", "mkv", "wmv", "flv", "webm")),
    FILE("기타 문서 파일", true, listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "hwp", "zip", "rar")),
    VOICE("음성 메시지", true, listOf("mp3", "wav", "m4a", "aac", "ogg", "flac")),
    EMOTICON("카카오톡 유료 이모티콘 (이모티콘 키워드)", false, emptyList()),
    MINI_EMOTICON("카카오톡 미니 이모티콘 (괄호 한글)", false, emptyList()),
    UNICODE_EMOJI("유니코드 이모지", false, emptyList()),
    LINK("URL 링크", false, emptyList()),
    SYSTEM("시스템 메시지", false, emptyList());

    companion object {
        private val miniEmoticonPattern = Regex("""\(([가-힣]{1,4})\)""")
        private val unicodeEmojiPattern = Regex("""[\uD83C-\uDBFF\uDC00-\uDFFF]+|[\u2600-\u27BF]+|[\u2B50]""")

        private val photoPattern = Regex("""^사진(\s+\d+장)?$""")
        private val videoPattern = Regex("""^동영상$""")
        private val voicePattern = Regex("""^음성메시지$|^음성 메시지$""")
        private val filePattern = Regex("""^파일$""")

        private val linkPattern = Regex("""https?://[^\s]+""")
        private val systemPattern = Regex("""님이 (들어왔습니다|나갔습니다)\.""")

        fun analyzeFromFilename(filename: String): ContentAnalysis {
            val extension = filename.substringAfterLast('.', "").lowercase()

            val type = ChatContentType.entries.firstOrNull { chatType ->
                extension in chatType.fileExtensions
            } ?: FILE

            return ContentAnalysis(
                types = setOf(type),
                primaryType = type,
                isDetailed = true,
                confidence = 1.0
            )
        }

        fun fromExtension(extension: String): ChatContentType? {
            val lowerExt = extension.lowercase()
            return ChatContentType.entries.firstOrNull { lowerExt in it.fileExtensions }
        }

        fun analyze(content: String): ContentAnalysis {
            val trimmed = content.trim()
            val detectedTypes = mutableSetOf<ChatContentType>()
            var confidence = 1.0

            if (trimmed.contains('.')) {
                val extension = trimmed.substringAfterLast('.', "").lowercase()
                val fileType = ChatContentType.entries.firstOrNull { extension in it.fileExtensions }

                if (fileType != null) {
                    return ContentAnalysis(
                        types = setOf(fileType),
                        primaryType = fileType,
                        isDetailed = true,
                        confidence = 1.0
                    )
                }
            }

            // 2. 카카오톡 유료 이모티콘 감지 (실험적)
            if (trimmed.startsWith("이모티콘 ")) {
                detectedTypes.add(EMOTICON)
                confidence = 0.8

                // 뒤에 텍스트가 있으면 TEXT도 추가
                val rest = trimmed.substring(4).trim()
                if (rest.isNotEmpty()) {
                    detectedTypes.add(TEXT)
                }
            } else if (trimmed == "이모티콘") {
                detectedTypes.add(EMOTICON)
                confidence = 0.8
            }

            // 3. 단순 텍스트 패턴 (사진, 동영상 등)
            val isPhotoOnly = photoPattern.matches(trimmed)
            val isVideoOnly = videoPattern.matches(trimmed)
            val isVoiceOnly = voicePattern.matches(trimmed)
            val isFileOnly = filePattern.matches(trimmed)

            if (isPhotoOnly) detectedTypes.add(PHOTO)
            if (isVideoOnly) detectedTypes.add(VIDEO)
            if (isVoiceOnly) detectedTypes.add(VOICE)
            if (isFileOnly) detectedTypes.add(FILE)

            // 단순 패턴이 감지되면 조기 반환
            if (detectedTypes.any { it in setOf(PHOTO, VIDEO, VOICE, FILE) } && detectedTypes.size == 1) {
                val primaryType = detectedTypes.first()
                return ContentAnalysis(
                    types = detectedTypes,
                    primaryType = primaryType,
                    isDetailed = false,
                    confidence = 1.0
                )
            }

            // 4. 시스템 메시지
            if (systemPattern.containsMatchIn(trimmed)) {
                detectedTypes.add(SYSTEM)
                return ContentAnalysis(
                    types = setOf(SYSTEM),
                    primaryType = SYSTEM,
                    isDetailed = false,
                    confidence = 1.0
                )
            }

            // 5. 미니 이모티콘 (고양이)
            if (miniEmoticonPattern.containsMatchIn(trimmed)) {
                detectedTypes.add(MINI_EMOTICON)
            }

            // 6. 유니코드 이모지
            if (unicodeEmojiPattern.containsMatchIn(trimmed)) {
                detectedTypes.add(UNICODE_EMOJI)
            }

            // 7. 링크
            if (linkPattern.containsMatchIn(trimmed)) {
                detectedTypes.add(LINK)
            }

            // 8. 텍스트 존재 여부 확인
            val contentWithoutSpecialChars = trimmed
                .replace(miniEmoticonPattern, "")
                .replace(unicodeEmojiPattern, "")
                .replace(linkPattern, "")
                .replace("이모티콘 ", "")
                .replace("이모티콘", "")
                .trim()

            if (contentWithoutSpecialChars.isNotEmpty()) {
                detectedTypes.add(TEXT)
            }

            // 9. 기본값: TEXT
            if (detectedTypes.isEmpty()) {
                detectedTypes.add(TEXT)
            }

            // 10. primaryType 결정
            val primaryType = when {
                PHOTO in detectedTypes -> PHOTO
                VIDEO in detectedTypes -> VIDEO
                FILE in detectedTypes -> FILE
                VOICE in detectedTypes -> VOICE
                TEXT in detectedTypes -> TEXT
                LINK in detectedTypes -> LINK
                EMOTICON in detectedTypes -> EMOTICON
                MINI_EMOTICON in detectedTypes -> MINI_EMOTICON
                UNICODE_EMOJI in detectedTypes -> UNICODE_EMOJI
                else -> TEXT
            }

            return ContentAnalysis(
                types = detectedTypes,
                primaryType = primaryType,
                isDetailed = false,
                confidence = confidence
            )
        }
    }
}