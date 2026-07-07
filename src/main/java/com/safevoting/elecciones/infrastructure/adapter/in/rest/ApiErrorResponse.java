package com.safevoting.elecciones.infrastructure.adapter.in.rest;

import java.time.LocalDateTime;

public record ApiErrorResponse(
        String errorCode,
        String message,
        String timestamp
) {
    public static ApiErrorResponse of(String errorCode, String message) {
        return new ApiErrorResponse(errorCode, message, LocalDateTime.now().toString());
    }
}
