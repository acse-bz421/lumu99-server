package com.lumu99.forum.common.api;

public record ApiError(
        String code,
        String message,
        String requestId
) {
}
