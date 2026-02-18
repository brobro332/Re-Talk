package xyz.re_talk.global.common.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException
import xyz.re_talk.global.common.filter.TraceIdFilter
import xyz.re_talk.global.common.response.ApiResponse
import xyz.re_talk.global.common.response.ErrorCode
import java.util.*

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ApiResponse<Unit>> {
        val traceId = TraceIdFilter.getCurrentTraceId()

        logger.warn("[traceId: $traceId] BusinessException: ${e.errorCode.code} - ${e.getDisplayMessage()}", e)

        val response = ApiResponse.fail<Unit>(
            errorCode = e.errorCode,
            message = e.getDisplayMessage(),
            details = e.details,
            traceId = traceId
        )

        return ResponseEntity.status(e.errorCode.status).body(response)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Unit>> {
        val traceId = TraceIdFilter.getCurrentTraceId()

        // 필드별 에러 메시지 수집
        val fieldErrors = e.bindingResult.allErrors.associate { error ->
            val fieldName = (error as? FieldError)?.field ?: "unknown"
            val message = error.defaultMessage ?: "검증 실패"
            fieldName to message
        }

        logger.warn("[traceId: $traceId] Validation failed: $fieldErrors")

        val response = ApiResponse.fail<Unit>(
            errorCode = ErrorCode.COMMON_INVALID_PARAMETER,
            message = "입력값 검증에 실패했습니다.",
            details = mapOf("fields" to fieldErrors),
            traceId = traceId
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    /**
     * 필수 파라미터 누락 예외 처리
     */
    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParameter(e: MissingServletRequestParameterException): ResponseEntity<ApiResponse<Unit>> {
        val traceId = TraceIdFilter.getCurrentTraceId()

        logger.warn("[traceId: $traceId] Missing parameter: ${e.parameterName}")

        val response = ApiResponse.fail<Unit>(
            errorCode = ErrorCode.COMMON_MISSING_PARAMETER,
            message = "필수 파라미터 '${e.parameterName}'가 누락되었습니다.",
            details = mapOf("parameter" to e.parameterName),
            traceId = traceId
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    /**
     * 파라미터 타입 불일치 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(e: MethodArgumentTypeMismatchException): ResponseEntity<ApiResponse<Unit>> {
        val traceId = TraceIdFilter.getCurrentTraceId()

        logger.warn("[traceId: $traceId] Type mismatch: ${e.name}")

        val response = ApiResponse.fail<Unit>(
            errorCode = ErrorCode.COMMON_INVALID_PARAMETER,
            message = "파라미터 '${e.name}'의 타입이 올바르지 않습니다.",
            details = mapOf(
                "parameter" to e.name,
                "expectedType" to (e.requiredType?.simpleName ?: "unknown")
            ),
            traceId = traceId
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    /**
     * HTTP 메서드 불일치 예외 처리
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(e: HttpRequestMethodNotSupportedException): ResponseEntity<ApiResponse<Unit>> {
        val traceId = TraceIdFilter.getCurrentTraceId()

        logger.warn("[traceId: $traceId] Method not supported: ${e.method}")

        val response = ApiResponse.fail<Unit>(
            errorCode = ErrorCode.COMMON_METHOD_NOT_ALLOWED,
            message = "지원하지 않는 HTTP 메서드입니다. (사용된 메서드: ${e.method})",
            details = mapOf(
                "method" to e.method,
                "supportedMethods" to (e.supportedHttpMethods?.map { it.name() } ?: emptyList())
            ),
            traceId = traceId
        )

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response)
    }

    /**
     * JSON 파싱 실패 예외 처리
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleMessageNotReadable(e: HttpMessageNotReadableException): ResponseEntity<ApiResponse<Unit>> {
        val traceId = TraceIdFilter.getCurrentTraceId()

        logger.warn("[traceId: $traceId] Message not readable: ${e.message}")

        val response = ApiResponse.fail<Unit>(
            errorCode = ErrorCode.COMMON_INVALID_PARAMETER,
            message = "요청 본문을 읽을 수 없습니다. JSON 형식을 확인해주세요.",
            traceId = traceId
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    /**
     * 리소스를 찾을 수 없음 예외 처리 (Spring Boot 3.2+)
     */
    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFound(e: NoResourceFoundException): ResponseEntity<ApiResponse<Unit>> {
        val traceId = TraceIdFilter.getCurrentTraceId()

        logger.warn("[traceId: $traceId] Resource not found: ${e.resourcePath}")

        val response = ApiResponse.fail<Unit>(
            errorCode = ErrorCode.COMMON_RESOURCE_NOT_FOUND,
            message = "요청한 리소스를 찾을 수 없습니다.",
            details = mapOf("path" to e.resourcePath),
            traceId = traceId
        )

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    /**
     * 기타 모든 예외 처리
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): ResponseEntity<ApiResponse<Unit>> {
        val traceId = TraceIdFilter.getCurrentTraceId()

        logger.error("[traceId: $traceId] Unexpected error occurred", e)

        val response = ApiResponse.fail<Unit>(
            errorCode = ErrorCode.COMMON_INTERNAL_SERVER_ERROR,
            message = "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
            details = if (isDevelopmentMode()) {
                mapOf(
                    "exceptionType" to e.javaClass.simpleName,
                    "exceptionMessage" to (e.message ?: "No message")
                )
            } else {
                null  // 운영 환경에서는 상세 에러 숨김
            },
            traceId = traceId
        )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }

    /**
     * 개발 모드 확인 (환경변수나 프로파일로 판단)
     */
    private fun isDevelopmentMode(): Boolean {
        val activeProfile = System.getProperty("spring.profiles.active") ?: ""
        return activeProfile.contains("dev") || activeProfile.contains("local")
    }
}


















