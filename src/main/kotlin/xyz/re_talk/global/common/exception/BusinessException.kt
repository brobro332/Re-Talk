package xyz.re_talk.global.common.exception

import xyz.re_talk.global.common.response.ErrorCode

open class BusinessException(
    val errorCode: ErrorCode,
    val customMessage: String? = null,
    val details: Map<String, Any>? = null
) : RuntimeException(customMessage ?: errorCode.message) {

    fun getDisplayMessage(): String = customMessage ?: errorCode.message
}

class ChatException(
    errorCode: ErrorCode,
    customMessage: String? = null,
    details: Map<String, Any>? = null
) : BusinessException(errorCode, customMessage, details)

class FileException(
    errorCode: ErrorCode,
    customMessage: String? = null,
    details: Map<String, Any>? = null
) : BusinessException(errorCode, customMessage, details)

class DatabaseException(
    errorCode: ErrorCode,
    customMessage: String? = null,
    details: Map<String, Any>? = null
) : BusinessException(errorCode, customMessage, details)

