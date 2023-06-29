package edu.npu.dto;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.UniqueElements;

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
        String positionLongitude,

        @NotNull
        String positionLatitude

) {
}
