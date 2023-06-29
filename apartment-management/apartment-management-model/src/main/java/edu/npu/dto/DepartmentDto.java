package edu.npu.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

/**
 * @Author: Yu
 * @Date: 2023/6/29
 */
public record DepartmentDto(
        @NotNull
        String name,

        @NotNull
        Integer isInterior,

        @NotNull
        Integer payType,

        @NotNull
        String position,

        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Double positionLongitude,

        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Double positionLatitude

) {
}
