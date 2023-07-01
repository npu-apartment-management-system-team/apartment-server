package edu.npu.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ApartmentPageQueryDto(
        @NotNull(message = "pageNum不能为空")
        Integer pageNum,
        @NotNull(message = "pageSize不能为空")
        Integer pageSize,
        String query,
        Double longitude,
        Double latitude
) {
}
