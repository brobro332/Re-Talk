package xyz.re_talk.global.common.response

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val code: String,
    val message: String,
    val status: HttpStatus
) {
    COMMON_INVALID_PARAMETER("COMM001", "잘못된 요청 파라미터입니다.", HttpStatus.BAD_REQUEST),
    COMMON_MISSING_PARAMETER("COMM002", "필수 파라미터가 누락되었습니다.", HttpStatus.BAD_REQUEST),
    COMMON_METHOD_NOT_ALLOWED("COMM003", "지원하지 않는 HTTP 메서드입니다.", HttpStatus.METHOD_NOT_ALLOWED),
    COMMON_INTERNAL_SERVER_ERROR("COMM004", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    COMMON_SERVICE_UNAVAILABLE("COMM005", "서비스를 일시적으로 사용할 수 없습니다.", HttpStatus.SERVICE_UNAVAILABLE),
    COMMON_RESOURCE_NOT_FOUND("COMM006", "요청한 리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    AUTH_TOKEN_EXPIRED("AUTH101", "인증 토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    AUTH_INVALID_TOKEN("AUTH102", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    AUTH_ACCESS_DENIED("AUTH103", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    AUTH_UNAUTHORIZED("AUTH104", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),

    CHAT_PARSING_FAILED("CHAT201", "채팅 파일 파싱에 실패했습니다.", HttpStatus.BAD_REQUEST),
    CHAT_INVALID_FORMAT("CHAT202", "지원하지 않는 채팅 파일 형식입니다.", HttpStatus.BAD_REQUEST),
    CHAT_ENCRYPTION_FAILED("CHAT203", "메시지 암호화에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    CHAT_DECRYPTION_FAILED("CHAT204", "메시지 복호화에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    CHAT_DUPLICATE_MESSAGE("CHAT205", "이미 존재하는 메시지입니다.", HttpStatus.CONFLICT),
    CHAT_MESSAGE_NOT_FOUND("CHAT206", "메시지를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CHAT_ROOM_NOT_FOUND("CHAT207", "채팅방을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    FILE_UPLOAD_FAILED("FILE301", "파일 업로드에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_SIZE_EXCEEDED("FILE302", "파일 크기가 제한을 초과했습니다.", HttpStatus.BAD_REQUEST),
    FILE_INVALID_EXTENSION("FILE303", "지원하지 않는 파일 확장자입니다.", HttpStatus.BAD_REQUEST),
    FILE_EMPTY("FILE304", "빈 파일은 업로드할 수 없습니다.", HttpStatus.BAD_REQUEST),
    FILE_EXTRACT_FAILED("FILE305", "압축 파일 해제에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_READ_FAILED("FILE306", "파일을 읽을 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    DB_CONNECTION_FAILED("DB401", "데이터베이스 연결에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    DB_QUERY_FAILED("DB402", "데이터베이스 쿼리 실행에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    DB_TRANSACTION_FAILED("DB403", "트랜잭션 처리에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    DB_DUPLICATE_KEY("DB404", "중복된 데이터가 존재합니다.", HttpStatus.CONFLICT),

    MQ_PUBLISH_FAILED("MQ501", "메시지 큐 발행에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    MQ_CONSUME_FAILED("MQ502", "메시지 큐 소비에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    MQ_CONNECTION_FAILED("MQ503", "메시지 큐 연결에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
}

