package com.inchinso.back.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 입력입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),

    // Auth
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "Refresh Token을 찾을 수 없습니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 사용자입니다."),
    NAME_REQUIRED(HttpStatus.BAD_REQUEST, "이름을 입력해주세요."),
    CANNOT_DEACTIVATE_SELF(HttpStatus.BAD_REQUEST, "자기 자신을 삭제할 수 없습니다."),

    // Club
    CLUB_NOT_FOUND(HttpStatus.NOT_FOUND, "소모임을 찾을 수 없습니다."),

    // Session
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "모임을 찾을 수 없습니다."),
    SESSION_FULL(HttpStatus.CONFLICT, "정원이 초과되었습니다."),
    SESSION_CLOSED(HttpStatus.BAD_REQUEST, "마감된 모임입니다."),

    // Participation
    ALREADY_PARTICIPATED(HttpStatus.CONFLICT, "이미 신청한 모임입니다."),
    PARTICIPATION_NOT_FOUND(HttpStatus.NOT_FOUND, "신청 내역을 찾을 수 없습니다."),
    NOT_PARTICIPATED(HttpStatus.BAD_REQUEST, "해당 모임에 신청하지 않았습니다."),

    // Court
    INVALID_COURT_NUMBER(HttpStatus.BAD_REQUEST, "코트 번호는 1~5 사이여야 합니다."),
    USER_NOT_IN_SESSION(HttpStatus.BAD_REQUEST, "해당 모임에 참가 신청한 회원만 코트 배정이 가능합니다."),

    // Notice
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "공지사항을 찾을 수 없습니다."),

    // S3
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
