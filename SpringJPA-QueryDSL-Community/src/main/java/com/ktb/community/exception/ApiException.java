package com.ktb.community.exception;

public class ApiException extends RuntimeException {
    private final ErrorCode errorCode;
    public ApiException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    public ApiException(ErrorCode errorCode, String detailMessage) {
        super(detailMessage == null ? errorCode.getMessage() : detailMessage);
        this.errorCode = errorCode;
    }
    public ErrorCode getErrorCode() { return errorCode; }
}
