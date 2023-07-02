package edu.npu.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * @author : [wangminan]
 * @description : [一句话描述该类的功能]
 */
public record UserApplicationDto(
        @NotNull(message = "申请类型不能为空")
        Integer type,
        @NotEmpty(message = "申请材料不能为空")
        String fileUrl
) {
}
