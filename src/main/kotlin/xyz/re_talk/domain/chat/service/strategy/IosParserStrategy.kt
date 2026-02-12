package xyz.re_talk.domain.chat.service.strategy

import org.springframework.stereotype.Component

@Component
class IosParserStrategy : ChatParserStrategy {
    private val messagePattern = """^(\d{4}\.\s*\d{1,2}\.\s*\d{1,2}\.\s*오[전|후]\s*\d{1,2}:\d{2}),\s*(.*?)\s*:\s*(.*)$""".toRegex()
    private val dateHeaderPattern = """^\d{4}년\s*\d{1,2}월\s*\d{1,2}일\s*.*요일$""".toRegex()

    override fun isSupport(line: String): Boolean {
        return line.contains(".") && line.contains(":") && (line.contains("오전") || line.contains("오후"))
    }

    override fun parse(line: String, roomId: String): ParserResult {
        val trimmedLine = line.trim()

        if (trimmedLine.isEmpty() || dateHeaderPattern.matches(trimmedLine)) return ParserResult.Skip

        val matchResult = messagePattern.find(line)
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