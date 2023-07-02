package edu.npu.dto;

import jakarta.validation.constraints.NotNull;

public record BasicQueryDto(
        @NotNull
        Integer pageNum,
        @NotNull
        Integer pageSize,
        String query
) {
}
