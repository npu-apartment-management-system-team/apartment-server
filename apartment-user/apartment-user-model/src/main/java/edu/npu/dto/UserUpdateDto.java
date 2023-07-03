package edu.npu.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UserUpdateDto(
        @NotEmpty
        String username,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @NotNull
        Long departmentId,
        @NotEmpty
        String name,
        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long personalId,
        @NotEmpty
        String personalCardUrl,
        @NotEmpty
        String faceUrl,
        String email,
        Integer sex,
        Integer isCadre
) {
}
