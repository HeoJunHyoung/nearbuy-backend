package io.github.junhyoung.nearbuy.global.exception;
import io.github.junhyoung.nearbuy.global.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalControllerAdvice {

    // BusinessException을 상속받는 모든 커스텀 예외를 일괄 처리
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode));
    }

    // Spring Security의 AccessDeniedException 처리
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity
                .status(ErrorCode.ACCESS_DENIED.getStatus())
                .body(ApiResponse.error(ErrorCode.ACCESS_DENIED));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception e) {
        // 서버에 어떤 오류가 발생했는지 상세한 로그
        log.error("[500] 서버 에러 메시지 : {}", e.getMessage(), e);

        // 클라이언트에게는 상세한 오류 내용 대신 일반적인 서버 오류 메시지 전송
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode));
    }
}