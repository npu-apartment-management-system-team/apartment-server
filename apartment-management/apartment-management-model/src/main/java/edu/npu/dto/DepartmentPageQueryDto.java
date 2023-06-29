package edu.npu.dto;

import jakarta.validation.constraints.NotNull;

/**
 * @Author: Yu
 * @Date: 2023/6/29
 */
public record DepartmentPageQueryDto(

        @NotNull
        Integer pageNum,

        @NotNull
        Integer pageSize,

        String query,

        String longitude,

        String latitude
) {
}
