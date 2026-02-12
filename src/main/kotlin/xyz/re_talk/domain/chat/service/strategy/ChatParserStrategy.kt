package xyz.re_talk.domain.chat.service.strategy

public interface ChatParserStrategy {
    fun isSupport(firstLine: String): Boolean
    fun parse(line: String, roomId: String): ParserResult
}

sealed class ParserResult {
    data class NewMessage(
        val sender: String,
        val sentAt: String,
        val content: String
    ) : ParserResult()
    object AppendToPrev : ParserResult()
    object Skip : ParserResult()
}

data class PendingMessage(
    val roomId: String,
    val sentAt: String,
    val sender: String,
    val contentBuffer: StringBuilder
) {
    fun appendContent(line: String) {
        contentBuffer.append("\n").append(line)
    }
}