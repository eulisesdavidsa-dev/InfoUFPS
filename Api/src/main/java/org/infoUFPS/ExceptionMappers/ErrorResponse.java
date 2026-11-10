package org.infoUFPS.ExceptionMappers;

import org.infoUFPS.Utils.BogotaTime;

import java.time.LocalDateTime;

public record ErrorResponse(
        String error,
        String message,
        int status,
        LocalDateTime timestamp
) {
    public static ErrorResponse of(String error, String message, int status) {
        return new ErrorResponse(error, message, status, BogotaTime.now());
    }
}
