package edu.npu.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UpdateBedDto(
        @NotNull(message = "房间id不能为空")
        Long roomId,
        @NotEmpty(message = "床位名称不能为空")
        String Name,
        Long receiptId,
        Integer isInUse
) {
}
