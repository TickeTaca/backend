package com.ticketaca.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final String status;
    private final T data;
    private final ErrorDetail error;
    private final LocalDateTime timestamp;

    private ApiResponse(String status, T data, ErrorDetail error) {
        this.status = status;
        this.data = data;
        this.error = error;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", data, null);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>("SUCCESS", null, null);
    }

    public static ApiResponse<Void> error(String code, String message) {
        return new ApiResponse<>("ERROR", null, new ErrorDetail(code, message));
    }

    @Getter
    public static class ErrorDetail {
        private final String code;
        private final String message;

        public ErrorDetail(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
