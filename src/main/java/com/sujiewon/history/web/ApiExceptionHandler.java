package com.sujiewon.history.web;

import com.sujiewon.history.service.InvalidSearchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(InvalidSearchException.class)
    public ResponseEntity<ErrorBody> invalidSearch(InvalidSearchException exception) {
        return ResponseEntity.badRequest().body(new ErrorBody(exception.getMessage()));
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ErrorBody> invalidDate() {
        return ResponseEntity.badRequest()
                .body(new ErrorBody("시작일과 종료일을 YYYY-MM-DD 형식으로 입력해 주세요."));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorBody> database(DataAccessException exception) {
        log.warn("데이터베이스 조회 실패", exception);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorBody("데이터베이스 조회에 실패했습니다. 접속 정보와 테이블을 확인해 주세요."));
    }

    public record ErrorBody(String message) {
    }
}
