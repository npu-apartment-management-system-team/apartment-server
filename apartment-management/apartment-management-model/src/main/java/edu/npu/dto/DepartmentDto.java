package edu.npu.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * @Author: Yu
 * @Date: 2023/6/29
 */
public record DepartmentDto(
        @NotNull(message = "班组长id不能为空")
        String name,

        @NotNull(message = "单位类型不能为空")
        Integer isInterior,

        @NotNull(message = "单位id不能为空")
        Integer payType,

        @NotNull(message = "位置不可为空")
        String position,

        @NotNull(message = "经度不能为空")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @DecimalMax(value = "180", message = "经度最大值为180")
        @DecimalMin(value = "-180", message = "经度最小值为-180")
        Double positionLongitude,

        @NotNull(message = "纬度不能为空")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @DecimalMax(value = "90", message = "纬度最大值为90")
        @DecimalMin(value = "-90", message = "纬度最小值为-90")
        Double positionLatitude

) {
}
