package com.jemari.common.exception;

import com.jemari.common.dto.ErrorDetail;
import org.springframework.http.HttpStatus;

import java.util.List;

public class AppException extends RuntimeException {
    private final HttpStatus status;
    private final List<ErrorDetail> errors;

    public AppException(String message, HttpStatus status, List<ErrorDetail> errors) {
        super(message);
        this.status = status;
        this.errors = errors;
    }

    public AppException(String message, HttpStatus status) {
        this(message, status, null);
    }

    public AppException(String message) {
        this(message, HttpStatus.BAD_REQUEST, null);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public List<ErrorDetail> getErrors() {
        return errors;
    }
}
