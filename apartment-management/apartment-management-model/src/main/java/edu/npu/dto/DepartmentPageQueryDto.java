package edu.npu.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

/**
 * @Author : Yu
 * @Date : 2023/6/29
 * @description : department查询分页dto
 */
public record DepartmentPageQueryDto(

        @NotNull
        Integer pageNum,

        @NotNull
        Integer pageSize,

        String query,

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Double longitude,

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Double latitude
) {
}
