package edu.npu.dto;

public record UpdatePasswordDto(
        String oldPassword,
        String newPassword
) {
}
