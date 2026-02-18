package xyz.re_talk.global.common.response

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorInfo? = null,
    val traceId: String = UUID.randomUUID().toString(),
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun <T> success(data: T, traceId: String = UUID.randomUUID().toString()): ApiResponse<T> {
            return ApiResponse(
                success = true,
                data = data,
                traceId = traceId
            )
        }

        fun success(traceId: String = UUID.randomUUID().toString()): ApiResponse<Unit> {
            return ApiResponse(
                success = true,
                data = Unit,
                traceId = traceId
            )
        }

        fun <T> fail(
            errorCode: ErrorCode,
            message: String? = null,
            details: Map<String, Any>? = null,
            traceId: String = UUID.randomUUID().toString()
        ): ApiResponse<T> {
            return ApiResponse(
                success = false,
                error = ErrorInfo(
                    code = errorCode.code,
                    message = message ?: errorCode.message,
                    details = details
                ),
                traceId = traceId
            )
        }

        fun <T> fail(
            errorInfo: ErrorInfo,
            traceId: String = UUID.randomUUID().toString()
        ): ApiResponse<T> {
            return ApiResponse(
                success = false,
                error = errorInfo,
                traceId = traceId
            )
        }
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorInfo(
    val code: String,
    val message: String,
    val details: Map<String, Any>? = null
)


