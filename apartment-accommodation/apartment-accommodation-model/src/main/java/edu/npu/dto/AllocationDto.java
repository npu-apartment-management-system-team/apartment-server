package edu.npu.dto;

import jakarta.validation.constraints.NotNull;

public record AllocationDto(
        @NotNull
        Long id,
        @NotNull
        Long bedId
) {
}
