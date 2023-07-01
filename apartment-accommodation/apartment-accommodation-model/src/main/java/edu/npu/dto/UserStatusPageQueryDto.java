package edu.npu.dto;

import jakarta.validation.constraints.NotNull;

public record UserStatusPageQueryDto(
        @NotNull(message = "页码不能为空")
        Integer pageNum,
        @NotNull(message = "每页条数不能为空")
        Integer pageSize
) {
}
