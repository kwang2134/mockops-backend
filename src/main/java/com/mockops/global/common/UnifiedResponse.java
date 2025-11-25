package com.mockops.global.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

import java.time.Instant;

@Getter
@JsonPropertyOrder({"success", "result"})
public class UnifiedResponse<T> {
    private final boolean success = true;
    private final T result;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private final Instant timestamp;

    private UnifiedResponse(T result) {
        this.result = result;
        this.timestamp = Instant.now();
    }

    public static <T> UnifiedResponse<T> success(T data) {
        return new UnifiedResponse<>(data);
    }

    public static <T> UnifiedResponse<T> success() {
        return new UnifiedResponse<>(null);
    }
}
