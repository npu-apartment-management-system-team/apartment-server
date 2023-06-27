package edu.npu.dto;

import jakarta.validation.constraints.NotNull;

/**
 * @author : [wangminan]
 * @description : [人证核身Dto]
 */
public record FaceVerificationDto (
        @NotNull
        String name,
        @NotNull
        String personalId,
        @NotNull
        String faceUrl
) {
}
