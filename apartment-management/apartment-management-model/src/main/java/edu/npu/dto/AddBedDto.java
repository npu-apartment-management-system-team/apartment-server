package edu.npu.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AddBedDto(
        @NotNull(message = "房间id不能为空")
        Long roomId,
        @NotEmpty(message = "床位名称不能为空")
        String bedName,
        @NotNull(message = "床位类型不能为空")
        Integer bedType,
        @NotNull(message = "床位状态不能为空")
        Integer bedStatus,
        @NotEmpty(message = "床位备注不能为空")
        String bedRemark
) {
}
