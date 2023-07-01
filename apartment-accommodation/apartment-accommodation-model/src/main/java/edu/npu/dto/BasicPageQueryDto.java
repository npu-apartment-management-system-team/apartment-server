package edu.npu.dto;

import jakarta.validation.constraints.NotNull;

public record BasicPageQueryDto(
        @NotNull
        Integer pageNum,
        @NotNull
        Integer pageSize
) {
}
