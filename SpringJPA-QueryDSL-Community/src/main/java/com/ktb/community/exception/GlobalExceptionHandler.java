package com.ktb.community.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.regex.Pattern;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<SimpleErrorResponse> handleApi(ApiException ex) {
        var ec = ex.getErrorCode();
        var body = new SimpleErrorResponse(ec.getStatus().value(),
                (ex.getMessage() == null || ex.getMessage().isBlank()) ? ec.getMessage() : ex.getMessage());
        return ResponseEntity.status(ec.getStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<SimpleErrorResponse> handleInvalid(MethodArgumentNotValidException ex) {
        var status = ErrorCode.VALIDATION_ERROR.getStatus();
        var body = new SimpleErrorResponse(status.value(), ErrorCode.VALIDATION_ERROR.getMessage());
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<SimpleErrorResponse> handleConstraint(ConstraintViolationException ex) {
        var status = ErrorCode.VALIDATION_ERROR.getStatus();
        var body = new SimpleErrorResponse(status.value(), ErrorCode.VALIDATION_ERROR.getMessage());
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<SimpleErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        var status = ErrorCode.MISSING_PARAMETER.getStatus();
        var body = new SimpleErrorResponse(status.value(), ErrorCode.MISSING_PARAMETER.getMessage());
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<SimpleErrorResponse> handleMissingHeader(MissingRequestHeaderException ex) {
        var status = ErrorCode.MISSING_HEADER.getStatus();
        var body = new SimpleErrorResponse(status.value(), ErrorCode.MISSING_HEADER.getMessage());
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<SimpleErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        var status = ErrorCode.TYPE_MISMATCH.getStatus();
        var body = new SimpleErrorResponse(status.value(), ErrorCode.TYPE_MISMATCH.getMessage());
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<SimpleErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        var status = ErrorCode.JSON_PARSE_ERROR.getStatus();
        var body = new SimpleErrorResponse(status.value(), ErrorCode.JSON_PARSE_ERROR.getMessage());
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<SimpleErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        var status = ErrorCode.METHOD_NOT_ALLOWED.getStatus();
        var body = new SimpleErrorResponse(status.value(), ErrorCode.METHOD_NOT_ALLOWED.getMessage());
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<SimpleErrorResponse> handleNoHandler(NoHandlerFoundException ex) {
        var status = ErrorCode.ROUTE_NOT_FOUND.getStatus();
        var body = new SimpleErrorResponse(status.value(), ErrorCode.ROUTE_NOT_FOUND.getMessage());
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<SimpleErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        String rootMsg = getDeepMostMessage(ex);
        ErrorCode ec = mapConstraintToErrorCode(rootMsg);
        var body = new SimpleErrorResponse(ec.getStatus().value(), ec.getMessage());
        return ResponseEntity.status(ec.getStatus()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<SimpleErrorResponse> handleOther(Exception ex, HttpServletRequest req) {
        var status = ErrorCode.INTERNAL_ERROR.getStatus();
        var body = new SimpleErrorResponse(status.value(), ErrorCode.INTERNAL_ERROR.getMessage());
        return ResponseEntity.status(status).body(body);
    }

    private static String getDeepMostMessage(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null) cur = cur.getCause();
        return cur.getMessage() == null ? t.toString() : cur.getMessage();
    }

    private static final Pattern EMAIL_UQ = Pattern.compile("uq_users_email", Pattern.CASE_INSENSITIVE);
    private static final Pattern NICK_UQ  = Pattern.compile("uq_users_nickname", Pattern.CASE_INSENSITIVE);

    private static ErrorCode mapConstraintToErrorCode(String msg) {
        if (msg == null) return ErrorCode.DB_ERROR;
        if (EMAIL_UQ.matcher(msg).find())   return ErrorCode.EMAIL_ALREADY_EXISTS;
        if (NICK_UQ.matcher(msg).find())    return ErrorCode.NICKNAME_ALREADY_EXISTS;
        return ErrorCode.CONFLICT;
    }
}
