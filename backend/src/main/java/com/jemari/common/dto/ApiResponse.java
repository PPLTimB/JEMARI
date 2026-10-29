package com.jemari.common.dto;

import java.util.List;

public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private List<ErrorDetail> errors;

    private ApiResponse(boolean success, String message, T data, List<ErrorDetail> errors) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errors = errors;
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, null);
    }

    public static <T> ApiResponse<T> fail(String message, List<ErrorDetail> errors) {
        return new ApiResponse<>(false, message, null, errors);
    }

     public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, null);
    }

    public static <T> ApiResponse<T> error(String message, List<ErrorDetail> errors) {
        return new ApiResponse<>(false, message, null, errors);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public List<ErrorDetail> getErrors() {
        return errors;
    }
}