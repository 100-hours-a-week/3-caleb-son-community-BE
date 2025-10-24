package com.ktb.community.exception;

public class SimpleErrorResponse {
    private final int status;
    private final String message;
    private final Object data;
    public SimpleErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.data = null;
    }
    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public Object getData() { return data; }
}
