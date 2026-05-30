package com.khush.notifiq.common;

import java.time.LocalDateTime;
import java.util.List;

public record ValidationErrorResponse(
        int status,
        String message,
        List<String> errors,
        LocalDateTime timestamp
) {
}