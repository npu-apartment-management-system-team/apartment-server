package edu.npu.dto;

import jakarta.validation.constraints.NotNull;

public record BedPageQueryDto(
        @NotNull
        Integer pageNum,
        @NotNull
        Integer pageSize,
        String query,
        Long apartmentId,
        Long roomId,
        Integer isInUse
) {
}
