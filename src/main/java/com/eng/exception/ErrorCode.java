package com.eng.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),

    //Word
    WORD_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 단어를 찾을 수 없습니다."),

    // Study
    STUDY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 학습 목록을 찾을 수 없습니다."),

    // db
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 에러");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
