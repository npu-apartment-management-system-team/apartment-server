package edu.npu.dto;

import edu.npu.entity.LoginAccount;
import jakarta.validation.constraints.NotNull;

/**
 * @Author: Yu
 * @Date: 2023.6.28
 */
public record AddAdminDto(
        @NotNull
        String username,
        @NotNull
        String password,
        @NotNull
        Integer role,
        @NotNull
        String name,
        @NotNull
        Long departmentId,
        String email
) {
}
