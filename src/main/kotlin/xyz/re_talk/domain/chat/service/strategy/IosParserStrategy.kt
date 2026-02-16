package xyz.re_talk.domain.chat.service.strategy

import org.springframework.stereotype.Component

@Component
class IosParserStrategy : ChatParserStrategy {
    private val messagePattern = """^(\d{4}\.\s*\d{1,2}\.\s*\d{1,2}\.\s*(?:오전|오후)\s*\d{1,2}:\d{2}),\s*(.*?)\s*[:：]\s*(.*)$""".toRegex()
    private val dateHeaderPattern = """^\d{4}년\s*\d{1,2}월\s*\d{1,2}일\s*.*요일$""".toRegex()

    override fun isSupport(firstLine: String): Boolean {
        val trimmed = firstLine.trim()
        return (trimmed.contains("오전") || trimmed.contains("오후")) &&
            Regex("""^\d{4}\.\s*\d{1,2}\.""").containsMatchIn(trimmed)
    }

    override fun parse(line: String): ParserResult {
        if (line.isBlank()) return ParserResult.Skip

        val trimmedLine = line.trim()

        if (dateHeaderPattern.matches(trimmedLine)) return ParserResult.Skip

        val matchResult = messagePattern.find(trimmedLine)
        if (matchResult != null) {
            val (sentAt, sender, content) = matchResult.destructured
            return ParserResult.NewMessage(
                sender = sender.trim(),
                sentAt = sentAt.trim(),
                content = content
            )
        }

        return ParserResult.AppendToPrev
    }
}