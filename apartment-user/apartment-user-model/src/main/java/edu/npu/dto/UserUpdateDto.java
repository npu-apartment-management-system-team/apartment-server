package edu.npu.dto;

public record UserUpdateDto(
        String username,
        String password,
        Long departmentId,
        String name,
        Long personalId,
        String personalCardUrl,
        String faceUrl,
        String email,
        Integer sex,
        Boolean isCadre
) {
}
