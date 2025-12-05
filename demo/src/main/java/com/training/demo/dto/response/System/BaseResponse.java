package com.training.demo.dto.response.System;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private Object errors;
    private String errorCode;
    private Long timestamp;

    public static <T> BaseResponse<T> success(T data, String message) {
        return BaseResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> BaseResponse<T> success(T data) {
        return BaseResponse.<T>builder()
                .success(true)
                .message("SUCCESS")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> BaseResponse<T> success() {
        return BaseResponse.<T>builder()
                .success(true)
                .message("SUCCESS")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> BaseResponse<T> failure(String message, Object errors) {
        return BaseResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    public static <T> BaseResponse<T> failure(String message, String errorCode, Object errors) {
        return BaseResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .errors(errors)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
