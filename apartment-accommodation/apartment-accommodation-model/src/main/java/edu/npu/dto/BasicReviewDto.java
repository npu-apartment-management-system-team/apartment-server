package edu.npu.dto;

import jakarta.validation.constraints.NotNull;

public record BasicReviewDto(
        @NotNull
        Long id,
        @NotNull
        Boolean pass
) {
}
