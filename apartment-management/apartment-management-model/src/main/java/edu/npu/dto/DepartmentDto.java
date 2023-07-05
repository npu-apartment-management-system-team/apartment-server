package edu.npu.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

/**
 * @Author: Yu
 * @Date: 2023/6/29
 */
public record DepartmentDto(
        @NotNull(message = "公寓名称不能为空")
        String name,

        @NotNull(message = "单位类型不能为空")
        Integer isInterior,

        @NotNull(message = "缴费类型不能为空")
        Integer payType,

        @NotNull(message = "位置不可为空")
        String position,

        @NotNull(message = "经度不能为空")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Double positionLongitude,

        @NotNull(message = "纬度不能为空")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Double positionLatitude

) {
}
