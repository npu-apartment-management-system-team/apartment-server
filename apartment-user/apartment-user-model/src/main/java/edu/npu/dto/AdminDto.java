package edu.npu.dto;

import jakarta.validation.constraints.NotNull;

/**
 * @Author : Yu
 * @Date : 2023.6.28
 * @description : [Admin表对应的dto]
 */
public record AdminDto(
        @NotNull
        String username,
        @NotNull
        String password,
        @NotNull
        Integer role,
        @NotNull
        String name,

        Long departmentId,
        String email
) {
}
