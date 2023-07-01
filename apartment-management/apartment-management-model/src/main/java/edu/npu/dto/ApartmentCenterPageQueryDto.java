package edu.npu.dto;

import jakarta.validation.constraints.NotNull;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */
public record ApartmentCenterPageQueryDto(
        @NotNull
        Integer pageNum,
        @NotNull
        Integer pageSize,
        @NotNull
        Long userId
) {
}
