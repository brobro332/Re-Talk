package xyz.re_talk.domain.chat.service.strategy

import org.springframework.stereotype.Component

@Component
class AndroidParserStrategy : ChatParserStrategy {
    private val messagePattern = """^(\d{4}년\s*\d{1,2}월\s*\d{1,2}일\s*(?:오전|오후)\s*\d{1,2}:\d{2}),\s*(.*?)\s*:\s*(.*)$""".toRegex()
    private val dateHeaderPattern = """^\d{4}년\s*\d{1,2}월\s*\d{1,2}일\s*(?:오전|오후)\s*\d{1,2}:\d{2}$""".toRegex()

    override fun isSupport(firstLine: String): Boolean {
        return firstLine.contains("년") && firstLine.contains("월") && firstLine.contains("일") && (firstLine.contains("오전") || firstLine.contains("오후"))
    }

    override fun parse(line: String): ParserResult {
        val trimmedLine = line.trim()
        if (trimmedLine.isEmpty() || dateHeaderPattern.matches(trimmedLine)) return ParserResult.Skip

        val matchResult = messagePattern.find(trimmedLine)
        if (matchResult != null) {
            val (dateTime, sender, content) = matchResult.destructured
            return ParserResult.NewMessage(sender, dateTime, content)
        }

        return ParserResult.AppendToPrev
    }
}