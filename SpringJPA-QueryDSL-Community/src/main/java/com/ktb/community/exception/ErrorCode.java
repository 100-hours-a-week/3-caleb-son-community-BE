package com.ktb.community.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "input_data_validation_failed"),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "input_data_validation_failed"),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "missing_parameter"),
    MISSING_HEADER(HttpStatus.BAD_REQUEST, "missing_header"),
    JSON_PARSE_ERROR(HttpStatus.BAD_REQUEST, "json_parse_error"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "token_not_valid"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "not_authorized"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "resource_not_found"),
    ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "route_not_found"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "method_not_allowed"),
    CONFLICT(HttpStatus.CONFLICT, "conflict"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "email_already_exists"),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "nickname_already_exists"),
    ALREADY_LIKED(HttpStatus.CONFLICT, "already_liked"),
    DB_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "db_error"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "internal_server_error"),
    TOKEN_NOT_VALID(HttpStatus.INTERNAL_SERVER_ERROR, "token_server_error"),
    NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "not_authorized");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status; this.message = message;
    }
    public HttpStatus getStatus() { return status; }
    public String getMessage() { return message; }
}
