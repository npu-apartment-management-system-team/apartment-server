package edu.npu.dto;

import jakarta.validation.constraints.NotNull;

/**
 * @Author: Yu
 * @Date: 2023.6.28
 */
public record PageQueryDto(
        @NotNull
        Integer pageNum,
        @NotNull
        Integer pageSize,
        String query,
        String departmentId
) {
}
