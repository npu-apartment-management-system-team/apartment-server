package edu.npu.dto;

import jakarta.validation.constraints.NotEmpty;

public record AddFaceDto(
        @NotEmpty
        String entityId,
        @NotEmpty
        String faceUrl
) {
}
